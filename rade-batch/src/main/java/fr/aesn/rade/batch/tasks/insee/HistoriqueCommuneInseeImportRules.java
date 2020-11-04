/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Marc Gimpel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* $Id$ */
package fr.aesn.rade.batch.tasks.insee;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fr.aesn.rade.common.InvalidArgumentException;
import fr.aesn.rade.common.util.StringConversionUtils;
import fr.aesn.rade.persist.model.Audit;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.service.CommuneService;
import fr.aesn.rade.service.MetadataService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Règles metier pour l'import de l'historique des Communes INSEE.
 *
 * Le point de départ du processus est le fichier "Historique des Communes" de
 * l'INSEE (voir https://www.insee.fr/fr/information/3363419#titre-bloc-11 ).
 * Chaque ligne de ce fichier relate un changement a une Commune avec un code
 * pour préciser le type de modification (MOD) et tous les détails.
 * Suivant le code du type de modification le processus est différent.
 *
 * Certaines modifications sont très basic et une ligne suffit de les décrire.
 * C'est le cas des modifications de types suivantes:
 * <ul>
 * <li>100: Changement de nom.</li>
 * <li>200: Création.</li>
 * <li>351: Commune nouvelle - suppression de la commune préexistante.</li>
 * </ul>
 * 
 * Certaines modifications sont à regrouper par paires.
 * Ex: pour une fusion de deux communes:
 * <ul>
 * <li>une ligne relate le changement à la commune absorbé (suppression).</li>
 * <li>une ligne relate le changement à la commune absorbante (modif.).</li>
 * </ul>
 * Les 2 lignes ont des codes de type de modification différentes mais sont
 * reliées par une autre champs de la Commune d'Echange (COMECH).
 * C'est le cas des modifications de types suivantes:
 * <ul>
 * <li>210-230: Retablissement et Commune se séparant.</li>
 * <li>350-360: Fusion-association se transformant en fusion simple.</li>
 * </ul>
 *
 * Certaines modifications sont à regrouper par ensemble paires reliés à un
 * même évenement/changement.
 * Ex: pour une fusion de plusieurs (plus que deux) communes
 * C'est le cas des modifications de types suivantes:
 * <ul>
 * <li>31-32: Fusion --
 *              NB: Chaque element 31 est regroupé avec un element 32 et vice
 *              versa. Les paires sont ensuite regroupé (COMECH du 32).</li>
 * <li>311-321: Fusion - Commune nouvelle sans déléguée --
 *              NB: L'element 311 peut être regroupé avec 321 ou 341, par
 *              contre 341 est uniquement regroupé avec 311 (et 312 ?).
 *              Les paires sont ensuite regroupé (COMECH du 321).</li>
 * <li>330-340: Fusion-association --
 *              NB: Chaque element 330 est regroupé avec un element 340 et vice
 *              versa. Les paires sont ensuite regroupé (COMECH du 340).</li>
 * <li>331-332-333-311-312-341: Fusion - Commune nouvelle avec déléguée --
 *              NB: Chaque element 331, 332, 333, 311 et 312 est regroupé avec
 *              un element 341, mais notons que pour 311 il ne s'agit pas de
 *              tous les elements car il peut aussi être regroupé avec 321.
 *              Les paires sont ensuite regroupé (COMECH du 341).
 *              Dans chaque ensemble il peut y avoir differents type de paires:
 *              331-341, 332-341, 333-341, 311-341 et 312-341.</li>
 * </ul>
 *
 * Enfin précisons que certaines modifications ne nous concernes pas.
 * C'est le cas des modifications de types suivantes:
 * <ul>
 * <li>...<li>
 * </ul>
 *
 * Il en resulte le tableau des actions suivantes:
 * <code>
 * |  Description                                                 |Code|Associé|Action
 * +--------------------------------------------------------------+----+-------+-------------------
 * |Chgt de nom                                                    100          Traitement ligne simple
 * |Chgt de nom dû à une fusion (simple ou association)            110  31/32 N/A (traité par 31-32)
 * |Chgt de nom (création de commune nouvelle)                     111  331/... N/A (traité par 331-332-333-311-312-341)
 * |Chgt de nom dû à un rétablissement                             120  210/230 N/A (traité par 210-230)
 * |Chgt de nom dû au chgt de chef-lieu                            130  ?       N/A
 * |Chgt de nom dû au transfert du bureau centr. de canton         140  ?       N/A
 * |Chgt de nom dû au transfert du chef-lieu d’arr.                150  ?       N/A
 * |Création                                                       200          Traitement ligne simple
 * |Rétablissement                                                 210  230     Traitement paire 210-230
 * |Commune ayant donné des parcelles pour la création             220  ?       N/A
 * |Commune se séparant                                            230  210     Voir 210
 * |Création d'une fraction cantonale                              240          N/A
 * |Suppression commune suite à partition de territoire            300          N/A
 * |Fusion: commune absorbée                                       31  32     Voir 32
 * |Commune nouv.: commune non déléguée                            311  321/341 Voir 321 et 341
 * |Commune nouv.: commune préexist. non délég. restant non délég. 312  341     Voir 341
 * |Fusion: commune absorbante                                     32  31     Traitement ensemble de paires 31-32 (regroupement par COMECH du 32)
 * |Commune nouv. sans délég.: commune-pôle                        321  311     Traitement ensemble de paires 311-321 (regroupement par COMECH du 321)
 * |Fusion - association: commune associée                         330  340     Voir 340
 * |Commune nouv.: commune délég.                                  331  341     Voir 341
 * |Commune nouv.: commune préexist. associée devenant délég.      332  341     Voir 341
 * |Commune nouv.: commune préexist. délég. restant délég.         333  341     Voir 341
 * |Fusion-association: commune absorbante                         340  330     Traitement ensemble de paires 31-32 (regroupement par COMECH du 340)
 * |Commune nouv. avec délég. : commune-pôle                       341  331     Traitement ensemble de paires 3xx-341 (regroupement par COMECH du 341)
 * |Fusion-assoc. se transf. en fusion simple (commune absorbée)   350  360     Voir 360
 * |Commune nouv.: suppression de commune préexistante             351          N/A
 * |Fusion-assoc. se transformant en fusion simple : commune-pôle  360  350     N/A
 * |Suppression de la fraction cantonale                           370          N/A
 * |Commune ayant reçu des parcelles suite à une suppression       390          N/A
 * |Chgt de région                                                 400          N/A
 * |Chgt de département                                            410          Traitement ligne simple (non utilisé depuis 1997)
 * |Chgt de département (lors de création de commune nouv.)        411          Traitement ligne simple
 * |Chgt d'arrondissement                                          420          N/A
 * |Chgt d'arrondissement (lors de création de commune nouv.)      421          N/A
 * |Chgt de canton                                                 430          N/A
 * |Chgt de canton (lors de création de commune nouv.)             431          N/A
 * |Transfert de chef-lieu de commune                              500          N/A
 * |Transfert de bureau centralisateur de canton                   510          N/A
 * |Transfert de chef-lieu d'arrondissement                        520          N/A
 * |Transfert de chef-lieu de département                          530
 * |Transfert de chef-lieu de région                               540
 * |Cession de parcelles avec incidence démographique              600          N/A
 * |Cession de parcelles sans incidence démographique              610          N/A
 * |Réception de parcelles avec incidence démographique            620          N/A
 * |Réception de parcelles sans incidence démographique            630          N/A
 * |Commune assoc. devenant délég. (hors création commune nouv.)   700          N/A
 * |Numéro ancien créé par erreur                                  990          N/A
 * </code>
 *
 * Notons aussi les points supplémentaires suivants:
 * <ul>
 * <li>Puisqu'une Commune peut subir plusieurs modifications à des dates
 *     différentes, il faut traiter les modifications dans l'ordre
 *     chronologique et pas par type de modification.</li>
 * </ul>
 *
 * L'algorithme the traitement du fichier historique est donc le suivant
 * <ol>
 * <li>Filtrer les lignes pour ne retenir que ceux qui couvre la periode de
 * traitement (colonne de la date effective EFF).</li>
 * <li>Identifier toutes les dates effectives distincts, regrouper les éléments
 * par ces dates et iterer les points suivants pour chaque group en ordre
 * chronologique.</li>
 * <li>Regrouper les elements en groupe correspondant au type de modification
 * (100: groupe d'éléments simples, 200: groupe d'éléments simples,
 * 210-230: groupe de paires, 31-32: groupe d'ensembles de paires,
 * 311-321: groupe d'ensembles de paires, ...).</li>
 * <li>Pour chaque regroupement, iterer a travers les éléments et appliquer
 * le traitement correspondant
 * (100: pour chaque ligne/élément simple, faites le changement de nom,
 * 200: pour chaque ligne/élément simple, faites la creation,
 * 210-230: pour chaque paire, faites la séparation-retablissement,
 * 31-32: pour chaque ensemble, faites la fusion de tous les communes,
 * ...).</li>
 * </ol>
 *
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
public class HistoriqueCommuneInseeImportRules {
  /** Service for Commune. */
  @Setter
  private CommuneService communeService;
  /** Service for Metadata. */
  @Setter
  private MetadataService metadataService;
  /** Audit details to add to Entity. */
  @Setter
  private Audit batchAudit;

  /**
   * Process all modifications in the list for records effective between the
   * given dates.
   * @param list List of INSEE Commune modifications.
   * @param start Start Date.
   * @param end End Date.
   * @throws InvalidArgumentException if any argument is null or invalid.
   */
  public void processAllMod(final List<HistoriqueCommuneInseeModel> list,
                            final Date start, final Date end)
    throws InvalidArgumentException {
    if (list == null || start == null || end == null) {
      throw new InvalidArgumentException("All arguments are mandatory.");
    }
    if (!start.before(end)) {
      throw new InvalidArgumentException("The Start Date must be before the End Date.");
    }
    log.debug("Processing all MODs, between {} and {}", start, end);
    processAllMod(filterListByDate(list, start, end));
  }

  /**
   * Process all modifications in the list..
   * @param list List of INSEE Commune modifications.
   * @throws InvalidArgumentException if any argument is null or invalid.
   */
  public void processAllMod(final List<HistoriqueCommuneInseeModel> list)
    throws InvalidArgumentException {
    if (list == null) {
      throw new InvalidArgumentException("The list argument is mandatory.");
    }
    List<Date> dates =
      HistoriqueCommuneInseeImportRules.buildDistinctSortedDateList(list);
    for (Date date : dates) {
      processAllMod(list, date);
    }
  }

  /**
   * Process all modifications in the list for records effective on the given
   * date.
   * @param list List of INSEE Commune modifications.
   * @param date The Effective Date.
   * @throws InvalidArgumentException if any argument is null or invalid.
   */
  public void processAllMod(final List<HistoriqueCommuneInseeModel> list, final Date date)
    throws InvalidArgumentException {
    if (list == null || date == null) {
      throw new InvalidArgumentException("All arguments are mandatory.");
    }
    log.debug("Processing all MODs, for {}", date);
    List<HistoriqueCommuneInseeModel> dateFilteredList =
      filterListByDate(list, date);
    // Order is important
    processModX10(dateFilteredList);
    processModX20(dateFilteredList);
    processModX30(dateFilteredList);
    processMod10(dateFilteredList);
    processMod411(dateFilteredList); // 411 before 3xx
    processMod200(dateFilteredList);
    processMod210x230(dateFilteredList);
    processMod31x32(dateFilteredList);
    processMod330x340(dateFilteredList);
    processMod311x321and331x332x333x341(dateFilteredList);
  }

  public void processMod10(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "10");
    log.debug("Processing MOD 10, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing elements: {}", historique);
      assert "10".equals(historique.getTypeEvenCommune()) : historique.getTypeEvenCommune();
      communeService.mod10ChangementdeNom(historique.getDateEffet(),
                                           batchAudit,
                                           historique.getCodeCommuneaprEven(),
                                           historique.getTypeNomClairAp(),
                                           historique.getNomClairTypographieRicheAp(),
                                           "");
    }
  }

  public void processMod200(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "200");
    log.debug("Processing MOD 200, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing element: {}", historique);
      assert "200".equals(historique.getTypeModification()) : historique.getTypeModification();
      communeService.mod200Creation(historique.getDateEffet(),
                                    batchAudit,
                                    historique.getCodeDepartement() + historique.getCodeCommune(),
                                    historique.getCodeDepartement(),
                                    historique.getTypeNomClair(),
                                    historique.getNomOfficiel(),
                                    "");
    }
  }

  public void processMod210x230(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel.Pair> pairList = buildModFilteredPairList(fullList, "210", "230");
    log.debug("Processing MOD 210 & 230, # of pairs: {}", pairList.size());
    for (HistoriqueCommuneInseeModel.Pair pair : pairList) {
      log.trace("Processing pair: {}", pair);
      assert pair.isValid();
      assert "210".equals(pair.getEnfant().getTypeModification()) : pair.getEnfant().getTypeModification();
      assert "230".equals(pair.getParent().getTypeModification()) : pair.getParent().getTypeModification();
    //TODO 
      //Commune com210retabli = buildCommune(pair.getEnfant());
      Commune com210retabli = buildCommuneEnfant(pair.getEnfant());
      Commune com230source = buildCommune(pair.getParent());
      communeService.mod210x230Retablissement(pair.getDateEffet(),
                                              batchAudit,
                                              com210retabli,
                                              com230source,
                                              null);
    }
  }

  public void processMod31x32(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel.Changeset> setList = buildModSet(buildModFilteredPairList(fullList, "31", "32"), "32");
    log.debug("Processing MOD 31 & 32, # of sets: {}", setList.size());
    for (HistoriqueCommuneInseeModel.Changeset set : setList) {
      log.trace("Processing set: {}", set);
      assert set.isValid();
      for(HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
    	  //TODO 
       // assert "310".equals(pair.getEnfant().getTypeModification()) : pair.getEnfant().getTypeModification();
        //assert "320".equals(pair.getParent().getTypeModification()) : pair.getParent().getTypeModification();
        assert "31".equals(pair.getEnfant().getTypeEvenCommune()) : pair.getEnfant().getTypeEvenCommune();
        assert "32".equals(pair.getParent().getTypeEvenCommune()) : pair.getParent().getTypeEvenCommune();
      }
      List<Commune> com31absorbe = buildCommuneList(buildEnfantList(set));
      Commune com32absorbant = buildCommune(set.getPairs().get(0).getParent());
      Commune test;
      for (HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        // Verify all parents have the same details
        test = buildCommune(pair.getParent());
        assert com32absorbant.equals(test);
      }
      communeService.mod31x32Fusion(set.getDateEffet(),
                                      batchAudit,
                                      com31absorbe,
                                      com32absorbant,
                                      null);
    }
  }

  public void processMod330x340(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel.Changeset> setList = buildModSet(buildModFilteredPairList(fullList, "330", "340"), "340");
    log.debug("Processing MOD 330 & 340, # of sets: {}", setList.size());
    for (HistoriqueCommuneInseeModel.Changeset set : setList) {
      log.trace("Processing set: {}", set);
      assert set.isValid();
      for(HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        assert "330".equals(pair.getEnfant().getTypeModification()) : pair.getEnfant().getTypeModification();
        assert "340".equals(pair.getParent().getTypeModification()) : pair.getParent().getTypeModification();
      }
      List<Commune> com330associe = buildCommuneList(buildEnfantList(set));
      Commune com340absorbant = buildCommune(set.getPairs().get(0).getParent());
      Commune test;
      for (HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        // Verify all parents have the same details
        test = buildCommune(pair.getParent());
        assert com340absorbant.equals(test);
      }
      communeService.mod330x340FusionAssociation(set.getDateEffet(),
                                                 batchAudit,
                                                 com330associe,
                                                 com340absorbant,
                                                 null);
    }
  }

  public void processMod311x321and331x332x333x341(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    Pair<List<HistoriqueCommuneInseeModel.Pair>, List<HistoriqueCommuneInseeModel.Pair>> lists = buildMod311x321and331x332x333x341list(fullList);
    processMod311x321(buildMod311x321set(lists.getLeft()));
    processMod331x332x333x341(buildMod331x332x333x341set(lists.getRight()));
  }

  private void processMod311x321(final List<HistoriqueCommuneInseeModel.Changeset> setList)
    throws InvalidArgumentException {
    log.debug("Processing MOD 311 & 321, # of sets: {}", setList.size());
    for (HistoriqueCommuneInseeModel.Changeset set : setList) {
      log.trace("Processing set: {}", set);
      assert set.isValid();
      for(HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        assert "311".equals(pair.getEnfant().getTypeModification()) : pair.getEnfant().getTypeModification();
        assert "321".equals(pair.getParent().getTypeModification()) : pair.getParent().getTypeModification();
      }
      List<Commune> com311 = buildCommuneList(buildEnfantList(set));
      Commune com321nouvelle = buildCommune(set.getPairs().get(0).getParent());
      Commune test;
      for (HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        // Verify all parents have the same details
        test = buildCommune(pair.getParent());
        assert com321nouvelle.equals(test);
      }
      communeService.mod311x321FusionSansDeleguee(set.getDateEffet(),
                                                  batchAudit,
                                                  com311,
                                                  com321nouvelle,
                                                  null);
    }
  }

  private void processMod331x332x333x341(final List<HistoriqueCommuneInseeModel.Changeset> setList)
    throws InvalidArgumentException {
    log.debug("Processing MOD 331 & 332 & 333 & 341, # of sets: {}", setList.size());
    for (HistoriqueCommuneInseeModel.Changeset set : setList) {
      log.trace("Processing set: {}", set);
      assert set.isValid();
      for(HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        assert "341".equals(pair.getParent().getTypeModification()) : pair.getParent().getTypeModification();
      }
      List<Commune> com331x332x333 = buildCommuneListWithModComment(buildEnfantList(set));
      Commune com341nouvelle = buildCommune(set.getPairs().get(0).getParent());
      Commune test;
      for (HistoriqueCommuneInseeModel.Pair pair : set.getPairs()) {
        // Verify all parents have the same details
        test = buildCommune(pair.getParent());
        assert com341nouvelle.equals(test);
      }
      communeService.mod331x332x333x341FusionAvecDeleguee(set.getDateEffet(),
                                                          batchAudit,
                                                          com331x332x333,
                                                          com341nouvelle,
                                                          null);
    }
  }

  public void processMod411(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "411");
    log.debug("Processing MOD 411, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing element: {}", historique);
      assert "411".equals(historique.getTypeModification()) : historique.getTypeModification();
      communeService.mod411ChangementDept(historique.getDateEffet(),
                                          batchAudit,
                                          historique.getCodeDepartement() + historique.getCodeCommune(),
                                          historique.getCodeDepartement(),
                                          historique.getAncienCommuneChgmtDept(),
                                          null);
    }
  }

  public void processModX10(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "X10");
    log.debug("Processing MOD X10, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing elements: {}", historique);
      assert "X10".equals(historique.getTypeModification()) : historique.getTypeModification();
      communeService.modX10ChangementdeNom(historique.getDateEffet(),
                                           batchAudit,
                                           historique.getCodeDepartement() + historique.getCodeCommune(),
                                           historique.getTypeNomClair(),
                                           historique.getNomOfficiel(),
                                           "");
    }
  }

  public void processModX20(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "X20");
    log.debug("Processing MOD X20, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing element: {}", historique);
      assert "X20".equals(historique.getTypeModification()) : historique.getTypeModification();
      communeService.modX20Creation(historique.getDateEffet(),
                                    batchAudit,
                                    historique.getCodeDepartement() + historique.getCodeCommune(),
                                    historique.getCodeDepartement(),
                                    historique.getTypeNomClair(),
                                    historique.getNomOfficiel(),
                                    "");
    }
  }

  public void processModX30(final List<HistoriqueCommuneInseeModel> fullList)
    throws InvalidArgumentException {
    List<HistoriqueCommuneInseeModel> list = buildModFilteredList(fullList, "X30");
    log.debug("Processing MOD X30, # of elements: {}", list.size());
    for (HistoriqueCommuneInseeModel historique : list) {
      log.trace("Processing element: {}", historique);
      assert "X30".equals(historique.getTypeModification()) : historique.getTypeModification();
      communeService.modX30Suppression(historique.getDateEffet(),
                                       batchAudit,
                                       historique.getCodeDepartement() + historique.getCodeCommune(),
                                       null);
    }
  }

  private Commune buildCommune(final HistoriqueCommuneInseeModel historique) {
    Commune commune = new Commune();
		    commune.setTypeEntiteAdmin(metadataService.getTypeEntiteAdmin("COM"));
		    commune.setDebutValidite(historique.getDateEffet());
		    commune.setCodeInsee(historique.getCodeCommuneaprEven());
		    commune.setDepartement(historique.getCodeCommuneaprEven().substring(0, 2));
		    commune.setTypeNomClair(metadataService.getTypeNomClair(historique.getTypeNomClairAp()));
		    commune.setNomEnrichi(historique.getNomClairTypographieRicheAp());
		    commune.setNomMajuscule(historique.getNomClairMajAp());
        
    return commune;
  }
  /**
   * 
   * @param historique
   * @return
   */
  private Commune buildCommuneEnfant(final HistoriqueCommuneInseeModel historique) {
	    Commune commune = new Commune();
			    commune.setTypeEntiteAdmin(metadataService.getTypeEntiteAdmin("COM"));
			    commune.setDebutValidite(historique.getDateEffet());
			    commune.setCodeInsee(historique.getCodeCommuneAvantEven());
			    commune.setDepartement(historique.getCodeCommuneAvantEven().substring(0, 2));
			    commune.setTypeNomClair(metadataService.getTypeNomClair(historique.getTypeNomClairAv()));
			    commune.setNomEnrichi(historique.getNomClairTypographieRicheAv());
			    commune.setNomMajuscule(historique.getNomClairMajAv());
	    	    
	    return commune;
	  }

  private List<Commune> buildCommuneList(final List<HistoriqueCommuneInseeModel> historiqueList) {
    List<Commune> list = new ArrayList<>(historiqueList.size());
    for (HistoriqueCommuneInseeModel historique : historiqueList) {
      list.add(buildCommune(historique));
    }
    return list;
  }

  private List<Commune> buildCommuneListWithModComment(final List<HistoriqueCommuneInseeModel> historiqueList) {
    List<Commune> list = new ArrayList<>(historiqueList.size());
    Commune commune;
    for (HistoriqueCommuneInseeModel historique : historiqueList) {
      commune = buildCommune(historique);
      commune.setCommentaire("MOD=" + historique.getTypeModification());
      list.add(commune);
    }
    return list;
  }

  /**
   * Build a list of all the children in the changeset.
   * @param set the changeset.
   * @return a list of all the children in the changeset.
   */
  private List<HistoriqueCommuneInseeModel> buildEnfantList(final HistoriqueCommuneInseeModel.Changeset set) {
    List<HistoriqueCommuneInseeModel.Pair> pairs = set.getPairs();
    List<HistoriqueCommuneInseeModel> list = new ArrayList<>(pairs.size());
    for (HistoriqueCommuneInseeModel.Pair pair : pairs) {
      list.add(pair.getEnfant());
    }
  return list;
  }

  private static final PairListWithLeftovers buildMod311x321list(final List<HistoriqueCommuneInseeModel> list) {
    PairListWithLeftovers pairlist = buildModFilteredPairListWithLeftovers(list, "311", "321");
    assert pairlist.mod2leftoverlist.isEmpty();
    return pairlist;
  }

  private static final List<HistoriqueCommuneInseeModel.Pair> buildMod331x332x333x341list(final List<HistoriqueCommuneInseeModel> list,
                                                                                          final List<HistoriqueCommuneInseeModel> mod311leftoverlist) {
    PairListWithLeftovers pairListWithLeftovers = buildModFilteredPairListWithLeftovers(list, "331", "341");
    assert pairListWithLeftovers.mod1leftoverlist.isEmpty();
    List<HistoriqueCommuneInseeModel> mod332list = buildModFilteredList(list, "332");
    pairListWithLeftovers = buildModFilteredPairListWithLeftovers(pairListWithLeftovers.pairlist, mod332list, pairListWithLeftovers.mod2leftoverlist);
    assert pairListWithLeftovers.mod1leftoverlist.isEmpty();
    List<HistoriqueCommuneInseeModel> mod333list = buildModFilteredList(list, "333");
    pairListWithLeftovers = buildModFilteredPairListWithLeftovers(pairListWithLeftovers.pairlist, mod333list, pairListWithLeftovers.mod2leftoverlist);
    assert pairListWithLeftovers.mod1leftoverlist.isEmpty();
    List<HistoriqueCommuneInseeModel> mod312list = buildModFilteredList(list, "312");
    pairListWithLeftovers = buildModFilteredPairListWithLeftovers(pairListWithLeftovers.pairlist, mod312list, pairListWithLeftovers.mod2leftoverlist);
    assert pairListWithLeftovers.mod1leftoverlist.isEmpty();
    if (mod311leftoverlist != null && !mod311leftoverlist.isEmpty()) {
      pairListWithLeftovers = buildModFilteredPairListWithLeftovers(pairListWithLeftovers.pairlist, mod311leftoverlist, pairListWithLeftovers.mod2leftoverlist);
      assert pairListWithLeftovers.mod1leftoverlist.isEmpty();
    }
    assert pairListWithLeftovers.mod2leftoverlist.isEmpty();
    return pairListWithLeftovers.pairlist;
  }

  public static final Pair<List<HistoriqueCommuneInseeModel.Pair>, List<HistoriqueCommuneInseeModel.Pair>> buildMod311x321and331x332x333x341list(final List<HistoriqueCommuneInseeModel> list) {
    PairListWithLeftovers mod311x321List = buildMod311x321list(list);
    return new ImmutablePair<>(mod311x321List.pairlist,
                               buildMod331x332x333x341list(list, mod311x321List.mod1leftoverlist));
  }

  public static final List<HistoriqueCommuneInseeModel> buildModFilteredList(final List<HistoriqueCommuneInseeModel> list, final String mod) {
    List<HistoriqueCommuneInseeModel> modlist = list.stream()
              .filter(history -> history.getTypeEvenCommune().equals(mod))
              .collect(Collectors.toList());
    log.debug("Filtered List MOD={} size: {}.", mod, modlist.size());
    return modlist;
  }

  public static final List<HistoriqueCommuneInseeModel.Pair> buildModFilteredPairList(final List<HistoriqueCommuneInseeModel> list, final String mod1, final String mod2) {
    List<HistoriqueCommuneInseeModel> mod1list = buildModFilteredList(list, mod1);
    List<HistoriqueCommuneInseeModel> mod2list = buildModFilteredList(list, mod2);
    assert mod1list.size() == mod2list.size();
    List<HistoriqueCommuneInseeModel.Pair> pairlist = new ArrayList<>(mod1list.size());
    List<HistoriqueCommuneInseeModel> parent;
    log.debug("Filtered Pair List MOD={}-{} size: {}", mod1, mod2, mod1list.size());
    for (HistoriqueCommuneInseeModel m1 : mod1list) {
      parent = mod2list.stream()
              .filter(h -> m1.getDateEffet().equals(h.getDateEffet())
                        && m1.getNomClairTypographieRicheAvecArticleAp().equals(h.getNomClairTypographieRicheAvecArticleAp())
                        && m1.getCodeCommuneaprEven().equals(h.getCodeCommuneaprEven())
                        && h.getCodeCommuneaprEven().equals(m1.getCodeCommuneaprEven()))
              .collect(Collectors.toList());
    //TODO 
      
      /**
       *       parent = mod2list.stream()
              .filter(h -> m1.getDateEffet().equals(h.getDateEffet())
                        && m1.getTexteLegislative().equals(h.getTexteLegislative())
                        && m1.getCommuneEchange().equals(h.getCodeDepartement() + h.getCodeCommune())
                        && h.getCommuneEchange().equals(m1.getCodeDepartement() + m1.getCodeCommune()))
              .collect(Collectors.toList());
       */
      
      assert parent.size() == 1;
      HistoriqueCommuneInseeModel.Pair pair =
        new HistoriqueCommuneInseeModel.Pair(parent.get(0), m1);
      assert pair.isValid() : pair;
      pairlist.add(pair);
      log.trace("found MOD-{}-{} pair: {} & {}", mod1, mod2, pair.getEnfant(), pair.getParent());
    }
    return pairlist;
  }

  private static final PairListWithLeftovers buildModFilteredPairListWithLeftovers(final List<HistoriqueCommuneInseeModel> list, final String mod1, final String mod2) {
    List<HistoriqueCommuneInseeModel> mod1list = buildModFilteredList(list, mod1);
    List<HistoriqueCommuneInseeModel> mod2list = buildModFilteredList(list, mod2);
    log.debug("Filtered Pair List MOD={}-{} sizes: {}-{}", mod1, mod2, mod1list.size(), mod2list.size());
    List<HistoriqueCommuneInseeModel.Pair> pairlist = new ArrayList<>(mod1list.size());
    return buildModFilteredPairListWithLeftovers(pairlist, mod1list, mod2list);
  }

  private static final PairListWithLeftovers buildModFilteredPairListWithLeftovers(final List<HistoriqueCommuneInseeModel.Pair> pairlist, final List<HistoriqueCommuneInseeModel> mod1list, final List<HistoriqueCommuneInseeModel> mod2list) {
    List<HistoriqueCommuneInseeModel> parent;
    List<HistoriqueCommuneInseeModel> mod1listprocessed = new ArrayList<>();
    for (HistoriqueCommuneInseeModel m1 : mod1list) {
      parent = mod2list.stream()
              .filter(h -> m1.getDateEffet().equals(h.getDateEffet())
                        && m1.getTexteLegislative().equals(h.getTexteLegislative())
                        && m1.getCommuneEchange().equals(h.getCodeDepartement() + h.getCodeCommune())
                        && h.getCommuneEchange().equals(m1.getCodeDepartement() + m1.getCodeCommune()))
              .collect(Collectors.toList());
      assert parent.size() <= 1;
      if (parent.size() == 1) {
        HistoriqueCommuneInseeModel.Pair pair =
          new HistoriqueCommuneInseeModel.Pair(parent.get(0), m1);
        assert pair.isValid() : pair;
        pairlist.add(pair);
        log.trace("Filtered Pair List MOD found pair: {} & {}", pair.getEnfant(), pair.getParent());
        mod1listprocessed.add(m1);
        mod2list.remove(parent.get(0));
      }
    }
    mod1list.removeAll(mod1listprocessed);
    PairListWithLeftovers result = new PairListWithLeftovers();
    result.pairlist = pairlist;
    result.mod1leftoverlist = mod1list;
    result.mod2leftoverlist = mod2list;
    return result;
  }

  public static final List<HistoriqueCommuneInseeModel.Changeset> buildMod311x321set(final List<HistoriqueCommuneInseeModel.Pair> list) {
    return buildModSet(list, "321");
  }

  public static final List<HistoriqueCommuneInseeModel.Changeset> buildMod331x332x333x341set(final List<HistoriqueCommuneInseeModel.Pair> list) {
    return buildModSet(list, "341");
  }

  public static final List<HistoriqueCommuneInseeModel.Changeset> buildModSet(final List<HistoriqueCommuneInseeModel.Pair> list, final String mod) {
    List<HistoriqueCommuneInseeModel.Changeset> setlist = new ArrayList<>();
    HistoriqueCommuneInseeModel.Changeset set;
    HistoriqueCommuneInseeModel.Pair pair;
    while (!list.isEmpty()) {
      pair = list.get(0);
      assert pair.getParent().getTypeEvenCommune().equals(mod);
      set = extractSet(list, pair);
      setlist.add(set);
      list.removeAll(set.getPairs());
    }
    return setlist;
  }

  private static final HistoriqueCommuneInseeModel.Changeset extractSet(final List<HistoriqueCommuneInseeModel.Pair> list,
                                                                        final HistoriqueCommuneInseeModel.Pair pair) {
    Date eff = pair.getDateEffet();
    String leg = pair.getEnfant().getTexteLegislative();
    String comech = pair.getEnfant().getCommuneEchange();
    List<HistoriqueCommuneInseeModel.Pair> set;
    set = list.stream()
              .filter(p -> p.getDateEffet().equals(eff)
                        && p.getEnfant().getTexteLegislative().equals(leg)
                        && p.getEnfant().getCommuneEchange().equals(comech))
              .collect(Collectors.toList());
    HistoriqueCommuneInseeModel.Changeset changeset = new HistoriqueCommuneInseeModel.Changeset();
    changeset.addAll(set);
    return changeset;
  }

  public static final List<HistoriqueCommuneInseeModel> filterListByDate(final List<HistoriqueCommuneInseeModel> list,
                                                                         final Date start,
                                                                         final Date end) {
    return list.stream()
               .filter(h -> !h.getDateEffet().before(start)
                            && h.getDateEffet().before(end))
               .collect(Collectors.toList());
  }

  public static final List<HistoriqueCommuneInseeModel> filterListByDate(final List<HistoriqueCommuneInseeModel> list,
                                                                         final Date date) {
    return list.stream()
               .filter(h -> h.getDateEffet().equals(date))
               .collect(Collectors.toList());
  }

  public static final List<Date> buildDistinctSortedDateList(final List<HistoriqueCommuneInseeModel> list) {
    Set<Date> dateset = new HashSet<>();
    for (HistoriqueCommuneInseeModel historique : list) {
      dateset.add(historique.getDateEffet());
    }
    return dateset.stream()
                  .sorted()
                  .collect(Collectors.toList());
  }

  /**
   * Pair List and LeftOver Lists.
   * @author Marc Gimpel (mgimpel@gmail.com)
   */
  private static final class PairListWithLeftovers {
    /** List of Paired off History elements. */
    private List<HistoriqueCommuneInseeModel.Pair> pairlist;
    /** List of unpaired elements from first MOD. */ 
    private List<HistoriqueCommuneInseeModel> mod1leftoverlist;
    /** List of unpaired elements from second MOD. */ 
    private List<HistoriqueCommuneInseeModel> mod2leftoverlist;
  }
}
