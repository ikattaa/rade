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
package fr.aesn.rade.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import fr.aesn.rade.common.InvalidArgumentException;
import fr.aesn.rade.common.util.StringConversionUtils;
import fr.aesn.rade.persist.dao.CommuneJpaDao;
import fr.aesn.rade.persist.dao.GenealogieEntiteAdminJpaDao;
import fr.aesn.rade.persist.model.Audit;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.GenealogieEntiteAdmin;
import fr.aesn.rade.persist.model.GenealogieEntiteAdmin.ParentEnfant;
import fr.aesn.rade.persist.model.TypeGenealogieEntiteAdmin;
import fr.aesn.rade.persist.model.TypeNomClair;
import fr.aesn.rade.service.CommuneService;
import fr.aesn.rade.service.MetadataService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service Implementation for Commune.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Service
@Transactional
@NoArgsConstructor @Slf4j
public class CommuneServiceImpl
  implements CommuneService {
  /** Data Access Object for Commune. */
  @Autowired @Setter
  private CommuneJpaDao communeJpaDao;
  @Autowired @Setter
  private GenealogieEntiteAdminJpaDao genealogieEntiteAdminJpaDao;
  @Autowired @Setter
  private MetadataService metadataService;

  /**
   * List all Commune.
   * @return a List of all the Commune.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Commune> getAllCommune() {
    log.debug("Commune list requested");
    return communeJpaDao.findAll();
  }

  /**
   * List all Commune valid at the given date.
   * @param date the date at which the code was valid
   * @return a List of all the Commune.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Commune> getAllCommune(final Date date) {
    log.debug("Commune list requested for Date: date={}", date);
    Date testDate = (date == null ? new Date() : date);
    return communeJpaDao.findAllValidOnDate(testDate);
  }

  /**
   * Returns a List of all Commune from the given departement, resembling the
   * given name and valid at the given date.
   * @param dept the departement of the Communes.
   * @param nameLike a pattern to search for Communes with a name resembling.
   * @param date the date at which the Communes were valid.
   * @return a List of all Commune matching the given parameters.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Commune> getAllCommune(final String dept,
                                     final String nameLike,
                                     final Date date){
    log.debug("Commune list requested for Date, Department and Name: date={}, departement={}, name like={}",
              date, dept, nameLike);
    Date testDate = (date == null ? new Date() : date);
    if (StringUtils.isEmpty(dept) && StringUtils.isEmpty(nameLike))
      return communeJpaDao.findAllValidOnDate(testDate);
    else if(!StringUtils.isEmpty(dept) && StringUtils.isEmpty(nameLike))
      return communeJpaDao.findByDepartementValidOnDate(dept, testDate);
    else if(StringUtils.isEmpty(dept) && !StringUtils.isEmpty(nameLike))
      return communeJpaDao.findByNameLikeValidOnDate(nameLike, testDate);
    else if(!StringUtils.isEmpty(dept) && !StringUtils.isEmpty(nameLike))
      return communeJpaDao.findByDepartementAndNameLikeValidOnDate(dept, nameLike, testDate);
    return null;
  }

  /**
   * Returns a Map of all Commune indexed by ID.
   * @return a Map of all Commune indexed by ID.
   */
  @Override
  public Map<Integer, Commune> getCommuneMap() {
    log.debug("Commune map requested");
    List<Commune> list = getAllCommune();
    HashMap<Integer, Commune> map = new HashMap<>(list.size());
    for (Commune item : list) {
      map.put(item.getId(), item);
    }
    return map;
  }

  /**
   * Returns a Map of all Commune valid at the given date and indexed by code.
   * @param date the date at which the Commune are valid.
   * @return a Map of all Commune indexed by code INSEE.
   */
  @Override
  public Map<String, Commune> getCommuneMap(final Date date) {
    log.debug("Commune map requested for Date: date={}", date);
    List<Commune> list = getAllCommune(date);
    HashMap<String, Commune> map = new HashMap<>(list.size());
    for (Commune item : list) {
      map.put(item.getCodeInsee(), item);
    }
    return map;
  }

  /**
   * Get the Commune with the given ID.
   * @param id the Commune ID.
   * @return the Commune with the given ID.
   */
  @Override
  @Transactional(readOnly = true)
  public Commune getCommuneById(final int id) {
    log.debug("Commune requested by ID: ID={}", id);
    Optional<Commune> result = communeJpaDao.findById(id);
    if (result.isPresent()) {
      return result.get();
    } else {
      return null;
    }
  }

  /**
   * Get the Commune with the given code.
   * @param code the Commune code.
   * @return list of Communes that have historically had the given code.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Commune> getCommuneByCode(final String code) {
    log.debug("Commune requested by code: code={}", code);
    return communeJpaDao.findByCodeInsee(code);
    /*
    // Can also be done by using an Example:
    Commune criteria = new Commune();
    criteria.setCodeInsee(code);
    Example<Commune> example = Example.of(criteria);
    return communeJpaDao.findAll(example);
    */
  }

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  @Override
  @Transactional(readOnly = true)
  public Commune getCommuneByCode(final String code, final Date date) {
    log.debug("Commune requested by code and date: code={}, date={}", code, date);
    Date testDate = (date == null ? new Date() : date);
    return communeJpaDao.findByCodeInseeValidOnDate(code, testDate);
  }

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  @Override
  @Transactional(readOnly = true)
  public Commune getCommuneByCode(final String code, final String date) {
    log.debug("Commune requested by code and date: code={}, date={}", code, date);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return getCommuneByCode(code, sdf.parse(date));
    } catch (ParseException e) {
      log.warn("Commune requested by code and date: Exception parsing date {}", date, e);
      return null;
    }
  }

  /**
   * Invalidates the given commune by setting the communes finValidite
   * field to the given date.
   * @param commune the commune to invalidate.
   * @param date the date of end of validity for the commune.
   * @return the now invalidated commune.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune invalidateCommune(final Commune commune,
                                   final Date date) {
    log.debug("Invalidate Commune requested: commune={}, date={}", commune, date);
    if ((commune == null) || (date == null)) {
      return null;
    }
    Commune oldCommune = getCommuneById(commune.getId());
    if (!(commune.equals(oldCommune))
        || (oldCommune.getFinValidite() != null)) {
      // given commune has other changes
      return null;
    }
    if (!(date.after(oldCommune.getDebutValidite()))) {
      // given end of validity if before commune beginning of validity
      return null;
    }
    oldCommune.setFinValidite(date);
    return communeJpaDao.save(oldCommune);
  }

  /**
   * Changes the name (MOD=100 : Changement de Nom) of the Commune with the
   * given CodeInsee effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of Commune to change.
   * @param tnccoff the type of the official new name.
   * @param nccoff the official new name.
   * @param commentaire comment for the genealogie link.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod10ChangementdeNom(final Date dateEffective,
                                       final Audit audit,
                                       final String codeInsee,
                                       final String tnccoff,
                                       final String nccoff,
                                       final String commentaire)
    throws InvalidArgumentException {
    return changeCommuneName(dateEffective,
                             audit,
                             codeInsee,
                             tnccoff,
                             nccoff,
                             commentaire,
                             "100");
  }

  /**
   * Creates (MOD=200 : Creation) a new Commune with the given CodeInsee and
   * details, effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of the new Commune.
   * @param departement the departement to which the new Commune belongs.
   * @param tnccoff the type of the official name.
   * @param nccoff the official name.
   * @param commentaire comment for the new Commune.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod200Creation(final Date dateEffective,
                                final Audit audit,
                                final String codeInsee,
                                final String departement,
                                final String tnccoff,
                                final String nccoff,
                                final String commentaire)
    throws InvalidArgumentException {
    return createCommune(dateEffective,
                         audit,
                         codeInsee,
                         departement,
                         tnccoff,
                         nccoff,
                         commentaire,
                         "200");
  }

  /**
   * Recreates (MOD=210 : Retablissement, MOD=230 : Commune se separant) the
   * given Commune from the given source Commune, effective as of the given
   * Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param com210retabli the new Commune.
   * @param com230source the source Commune.
   * @param commentaire comment for the genealogie link.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public void mod210x230Retablissement(final Date dateEffective,
                                       final Audit audit,
                                       final Commune com210retabli,
                                       final Commune com230source,
                                       final String commentaire)
    throws InvalidArgumentException {
    // validate arguments
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException(
              "The date and audit are mandatory.");
    }
    if (com210retabli == null || com210retabli.getCodeInsee() == null
            || com210retabli.getDepartement() == null
            || com210retabli.getTypeNomClair() == null
            || com210retabli.getNomEnrichi() == null) {
      throw new InvalidArgumentException(
              "A mandatory detail for Commune retabli (210) was null.");
    }
    if (com210retabli.getId() != null) {
      throw new InvalidArgumentException(
              "Commune rétabli has an ID already set.");
    }
    if (!dateEffective.equals(com210retabli.getDebutValidite())) {
      throw new InvalidArgumentException(
              "Commune rétabli is not valid from the give date.");
    }
    Commune commune = communeJpaDao.findByCodeInseeValidOnDate(com210retabli.getCodeInsee(), dateEffective);
    if ((commune != null)) {
      throw new InvalidArgumentException(
              "Commune rétabli already exists for the given date.");
    }
    if (com230source == null || com230source.getCodeInsee() == null
            || com230source.getDepartement() == null
            || com230source.getTypeNomClair() == null
            || com230source.getNomEnrichi() == null) {
      throw new InvalidArgumentException(
              "A mandatory Commune detail for Commune source (230) was null.");
    }
    log.info("Mod=210-230 (Retablissement) requested: date={}, code commune 210={}, code commune 230={}",
             dateEffective, com210retabli.getCodeInsee(), com230source.getCodeInsee());
    // update source commune (230).
    Commune parentSource = invalidateCommune(com230source.getCodeInsee(), dateEffective);
    com230source.setId(null);
    com230source.setDebutValidite(dateEffective);
    if (com230source.getNomMajuscule() == null) {
      com230source.setNomMajuscule(StringConversionUtils.toUpperAsciiWithLookup(com230source.getNomEnrichi()));
    }
    if (com230source.getArticleEnrichi() == null) {
      com230source.setArticleEnrichi(com230source.getTypeNomClair().getArticle());
    }
    if (com230source.getCommentaire() == null) {
        com230source.setCommentaire("");
    }
    com230source.setAudit(audit);
    Commune enfantSource = communeJpaDao.save(com230source);
    buildGenealogie(parentSource, enfantSource, "230", commentaire);
    // create new commune retabli
    com210retabli.setAudit(audit);
    if (com210retabli.getNomMajuscule() == null) {
      com210retabli.setNomMajuscule(StringConversionUtils.toUpperAsciiWithLookup(com210retabli.getNomEnrichi()));
    }
    if (com210retabli.getArticleEnrichi() == null && com210retabli.getTypeNomClair().getArticleMaj() != null) {
      com210retabli.setArticleEnrichi(com210retabli.getTypeNomClair().getArticle());
    }
    Commune enfantRetabli = communeJpaDao.save(com210retabli);
    // add genealogie
    buildGenealogie(parentSource, enfantRetabli, "210", commentaire);
  }

  /**
   * Merges (MOD=310 : Fusion Commune absorbe, MOD=320 : Fusion Commune
   * absorbante) the given Communes, effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param com310absorbe list of absorbed Commune.
   * @param com320absorbant the absorbing Commune.
   * @param commentaire comment for the genealogie link.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod310x320Fusion(final Date dateEffective,
                                  final Audit audit,
                                  final List<Commune> com310absorbe,
                                  final Commune com320absorbant,
                                  final String commentaire)
    throws InvalidArgumentException {
    return mergeCommunes(dateEffective,
                         audit,
                         com310absorbe,
                         com320absorbant,
                         commentaire,
                         "320");
  }

  /**
   * Merges (MOD=330 : Fusion-association Commune associee, MOD=340 : Fusion-
   * association Commune absorbante) the given Communes, effective as of the
   * given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param com330associe list of absorbed Commune.
   * @param com340absorbant the absorbing Commune.
   * @param commentaire comment for the genealogie link.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod330x340FusionAssociation(final Date dateEffective,
                                             final Audit audit,
                                             final List<Commune> com330associe,
                                             final Commune com340absorbant,
                                             final String commentaire)
    throws InvalidArgumentException {
    return mergeCommunes(dateEffective,
                         audit,
                         com330associe,
                         com340absorbant,
                         commentaire,
                         "340");
  }

  /**
   * Merges (MOD=311 : Commune nouvelle non deleguee, MOD=321 : Commune
   * nouvelle sans deleguee) the given Communes, effective as of the given
   * Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param com311 list of absorbed Commune.
   * @param com321nouvelle the new/absorbing Commune.
   * @param commentaire comment for the genealogie link.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod311x321FusionSansDeleguee(final Date dateEffective,
                                              final Audit audit,
                                              final List<Commune> com311,
                                              final Commune com321nouvelle,
                                              final String commentaire)
    throws InvalidArgumentException {
    return mergeCommunes(dateEffective,
                         audit,
                         com311,
                         com321nouvelle,
                         commentaire,
                         "321");
  }

  /**
   * Merges (MOD=331,332,333,311,312 : Commune absorbe, MOD=341 : Commune
   * nouvelle avec deleguee) the given Communes, effective as of the given
   * Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param com331x332x333 list of absorbed Commune.
   * @param com341nouvelle the new/absorbing Commune.
   * @param commentaire comment for the genealogie link.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod331x332x333x341FusionAvecDeleguee(final Date dateEffective,
                                                      final Audit audit,
                                                      final List<Commune> com331x332x333,
                                                      final Commune com341nouvelle,
                                                      final String commentaire)
    throws InvalidArgumentException {
    return mergeCommunes(dateEffective,
                         audit,
                         com331x332x333,
                         com341nouvelle,
                         commentaire,
                         "341");
  }

  /**
   * Changes the departement (MOD=411 : Changement de departement) that the
   * Commune belongs to (NB: this involves changing it's codeInsee).
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the new code of the Commune.
   * @param departement the new departement to which the Commune belongs.
   * @param oldCodeInsee the old code for the Commune.
   * @param commentaire comment for the genealogie link.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune mod411ChangementDept(final Date dateEffective,
                                      final Audit audit,
                                      final String codeInsee,
                                      final String departement,
                                      final String oldCodeInsee,
                                      final String commentaire)
    throws InvalidArgumentException {
    // validate arguments
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException("The date and audit are mandatory.");
    }
    if (codeInsee == null || departement == null || oldCodeInsee == null) {
      throw new InvalidArgumentException(
              "A mandatory detail was null");
    }
    Commune commune = getCommuneByCode(codeInsee, dateEffective);
    if ((commune != null)) {
      throw new InvalidArgumentException(
              "There is already a Commune with the given codeInsee valid at the dateEffective");
    }
    log.info("Mod=411 (Changement de Departement) requested: date={}, new code commune={}, old code commune={}",
             dateEffective, codeInsee, oldCodeInsee);
    // invalidate old commune
    Commune parent = invalidateCommune(oldCodeInsee, dateEffective);
    // create new commune
    Commune newCommune = buildCommune(codeInsee,
                                      departement,
                                      dateEffective,
                                      parent.getTypeNomClair(),
                                      parent.getNomEnrichi(),
                                      parent.getNomMajuscule(),
                                      null);
    newCommune.setAudit(audit);
    Commune enfant = communeJpaDao.save(newCommune);
    // add genealogie
    buildGenealogie(parent, enfant, "411", commentaire);
    return getCommuneById(enfant.getId());
  }

  /**
   * Changes the name (MOD=X10 : Changement d'orthographe) of the Commune with
   * the given CodeInsee effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of Commune to change.
   * @param tnccoff the type of the official new name.
   * @param nccoff the official new name.
   * @param commentaire comment for the genealogie link.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune modX10ChangementdeNom(final Date dateEffective,
                                       final Audit audit,
                                       final String codeInsee,
                                       final String tnccoff,
                                       final String nccoff,
                                       final String commentaire)
    throws InvalidArgumentException {
    return changeCommuneName(dateEffective,
                             audit,
                             codeInsee,
                             tnccoff,
                             nccoff,
                             commentaire,
                             "X10");
  }

  /**
   * Creates (MOD=X20 : Creation) a new Commune with the given CodeInsee and
   * details, effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of the new Commune.
   * @param departement the departement to which the new Commune belongs.
   * @param tnccoff the type of the official name.
   * @param nccoff the official name.
   * @param commentaire comment for the new Commune.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  @Override
  @Transactional(readOnly = false)
  public Commune modX20Creation(final Date dateEffective,
                                final Audit audit,
                                final String codeInsee,
                                final String departement,
                                final String tnccoff,
                                final String nccoff,
                                final String commentaire)
    throws InvalidArgumentException {
    return createCommune(dateEffective,
                         audit,
                         codeInsee,
                         departement,
                         tnccoff,
                         nccoff,
                         commentaire,
                         "X20");
  }

  /**
   * Removes the Commune (MOD=X30 : Suppression).
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of the Commune to remove.
   * @param commentaire comment.
   * @return the invalidated commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  public Commune modX30Suppression(Date dateEffective, Audit audit,
                                   String codeInsee, String commentaire)
    throws InvalidArgumentException {
    // validate arguments
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException("The date and audit are mandatory.");
    }
    if (codeInsee == null) {
      throw new InvalidArgumentException("The codeInsee is mandatory.");
    }
    Commune commune = getCommuneByCode(codeInsee, dateEffective);
    if ((commune == null)) {
      throw new InvalidArgumentException(
              "There is no Commune with the given codeInsee valid at the dateEffective");
    }
    log.info("Mod=XXX (Suppression) requested: date={}, code commune={}",
             dateEffective, codeInsee);
    // invalidate old commune
    return invalidateCommune(codeInsee, dateEffective);
  }

  /**
   * Changes the name of the Commune with the given CodeInsee effective as of
   * the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of Commune to change.
   * @param tnccoff the type of the official new name.
   * @param nccoff the official new name.
   * @param commentaire comment for the genealogie link.
   * @param mod code for the type of genealogie link.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune changeCommuneName(final Date dateEffective,
                                    final Audit audit,
                                    final String codeInsee,
                                    final String tnccoff,
                                    final String nccoff,
                                    final String commentaire,
                                    final String mod)
    throws InvalidArgumentException {
    // validate arguments
    if (mod == null) {
      throw new InvalidArgumentException(
              "Mod is mandatory.");
    }
    assert "X10".equals(mod) || mod.startsWith("1");
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException(
              "The date and audit are mandatory.");
    }
    if (codeInsee == null || tnccoff == null || nccoff == null) {
      throw new InvalidArgumentException(
              "A mandatory Commune detail was null");
    }
    log.info("Mod={} ({}) requested: commune={}, date={}",
             mod,
             metadataService.getTypeGenealogieEntiteAdmin(mod).getLibelleCourt(),
             codeInsee, dateEffective);
    // invalidate old commune
    Commune parent = invalidateCommune(codeInsee, dateEffective);
    // create new commune
    Commune newCommune = buildCommune(codeInsee,
                                      parent.getDepartement(),
                                      dateEffective,
                                      tnccoff,
                                      nccoff,
                                      null,
                                      null);
    newCommune.setAudit(audit);
    Commune enfant = communeJpaDao.save(newCommune);
    // add genealogie
    buildGenealogie(parent, enfant, mod, commentaire);
    return getCommuneById(enfant.getId());
  }

  /**
   * Creates a new Commune with the given CodeInsee and details, effective as
   * of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of the new Commune.
   * @param departement the departement to which the new Commune belongs.
   * @param tnccoff the type of the official name.
   * @param nccoff the official name.
   * @param commentaire comment for the new Commune.
   * @param mod code for the type of genealogie link.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune createCommune(final Date dateEffective,
                                final Audit audit,
                                final String codeInsee,
                                final String departement,
                                final String tnccoff,
                                final String nccoff,
                                final String commentaire,
                                final String mod)
    throws InvalidArgumentException {
    // validate arguments
    if (mod == null) {
      throw new InvalidArgumentException(
              "Mod is mandatory.");
    }
    assert "X20".equals(mod) || mod.startsWith("2");
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException(
              "The date and audit are mandatory.");
    }
    if (codeInsee == null || departement == null || tnccoff == null || nccoff == null) {
      throw new InvalidArgumentException(
              "A mandatory Commune detail was null");
    }
    Commune commune = getCommuneByCode(codeInsee, dateEffective);
    if ((commune != null)) {
      throw new InvalidArgumentException(
              "There is already a Commune with the given codeInsee valid at the dateEffective");
    }
    log.info("Mod={} ({}) requested: commune={}, date={}",
            mod,
            metadataService.getTypeGenealogieEntiteAdmin(mod).getLibelleCourt(),
            codeInsee, dateEffective);
    // create new commune
    Commune newCommune = buildCommune(codeInsee,
                                      departement,
                                      dateEffective,
                                      tnccoff,
                                      nccoff,
                                      null,
                                      commentaire);
    newCommune.setAudit(audit);
    return communeJpaDao.save(newCommune);
  }

  /**
   * Merges the given Communes, effective as of the given Date.
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param comAbsorbe list of absorbed Commune.
   * @param comAbsorbant the absorbing Commune.
   * @param commentaire comment for the genealogie link.
   * @param mod code for the type of genealogie link.
   * @return the newly merged Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune mergeCommunes(final Date dateEffective,
                             final Audit audit,
                             final List<Commune> comAbsorbe,
                             final Commune comAbsorbant,
                             final String commentaire,
                             final String mod)
    throws InvalidArgumentException {
    // validate arguments
    if (dateEffective == null || audit == null) {
      throw new InvalidArgumentException(
              "The date and audit are mandatory.");
    }
    if (comAbsorbe == null || comAbsorbe.isEmpty()) {
      throw new InvalidArgumentException(
              "Commune absorbé list cannot be null or empty.");
    }
    if (comAbsorbant == null || comAbsorbant.getCodeInsee() == null
            || comAbsorbant.getDepartement() == null
            || comAbsorbant.getTypeNomClair() == null
            || comAbsorbant.getNomEnrichi() == null) {
      throw new InvalidArgumentException(
              "A mandatory detail for Commune absorbant was null.");
    }
    if (comAbsorbant.getId() != null) {
      throw new InvalidArgumentException(
              "Commune absorbant has an ID already set.");
    }
    if (!dateEffective.equals(comAbsorbant.getDebutValidite())) {
      throw new InvalidArgumentException(
              "Commune absorbant is not valid from the give date.");
    }
    log.info("Mod={} ({}) requested: commune={}, date={}",
             mod,
             metadataService.getTypeGenealogieEntiteAdmin(mod).getLibelleCourt(),
             comAbsorbant.getCodeInsee(), dateEffective);
    // create new Commune absorbant
    Commune parentAbsorbant = invalidateCommune(comAbsorbant.getCodeInsee(), dateEffective);
    comAbsorbant.setAudit(audit);
    if (comAbsorbant.getNomMajuscule() == null) {
      comAbsorbant.setNomMajuscule(StringConversionUtils.toUpperAsciiWithLookup(comAbsorbant.getNomEnrichi()));
    }
    if (comAbsorbant.getArticleEnrichi() == null
            && comAbsorbant.getTypeNomClair().getArticleMaj() != null) {
      comAbsorbant.setArticleEnrichi(comAbsorbant.getTypeNomClair().getArticle());
    }
    Commune enfantAbsorbant = communeJpaDao.save(comAbsorbant);
    buildGenealogie(parentAbsorbant, enfantAbsorbant, mod, commentaire);
    Commune parentAbsorbe;
    // invalidate all Commune absorbe
    String modAbsorbe;
    for (Commune commune : comAbsorbe) {
      // for mod331x332x333x341 the MOD of the absorbed Communes is passed in
      // the comment because some are already invalidated (MOD=332 & MOD=333)
      modAbsorbe = commune.getCommentaire();
      if (comAbsorbant.getCodeInsee().equals(commune.getCodeInsee())) {
        log.trace("Already invalidated and genealogised commune: {}", comAbsorbant.getCodeInsee());
      } else if (modAbsorbe != null && ("MOD=332".equals(modAbsorbe) || "MOD=333".equals(modAbsorbe))) {
          log.trace("No need to invalidate commune: {}", commune);
      } else {
        parentAbsorbe = invalidateCommune(commune.getCodeInsee(), dateEffective);
        buildGenealogie(parentAbsorbe, enfantAbsorbant, mod, commentaire);
      }
    }
    return enfantAbsorbant;
  }

  /**
   * Create a Genealogic link between the given Communes.
   * @param parent the parent.
   * @param enfant the child.
   * @param type the type of Genealogic link.
   * @param commentaire a comment.
   * @return the newly created GenealogieEntiteAdmin Object.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private GenealogieEntiteAdmin buildGenealogie(final Commune parent,
                                                final Commune enfant,
                                                final String type,
                                                final String commentaire)
    throws InvalidArgumentException {
    return buildGenealogie(parent,
                           enfant,
                           metadataService.getTypeGenealogieEntiteAdmin(type),
                           commentaire);
  }

  /**
   * Create a Genealogic link between the given Communes.
   * @param parent the parent.
   * @param enfant the child.
   * @param type the type of Genealogic link.
   * @param commentaire a comment.
   * @return the newly created GenealogieEntiteAdmin Object.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private GenealogieEntiteAdmin buildGenealogie(final Commune parent,
                                                final Commune enfant,
                                                final TypeGenealogieEntiteAdmin type,
                                                final String commentaire)
    throws InvalidArgumentException {
    if (parent == null || enfant == null || type == null) {
      throw new InvalidArgumentException("The parent, child and type are mandatory.");
    }
    ParentEnfant parentEnfant = new ParentEnfant();
    parentEnfant.setParent(parent);
    parentEnfant.setEnfant(enfant);
    GenealogieEntiteAdmin genealogie = new GenealogieEntiteAdmin();
    genealogie.setParentEnfant(parentEnfant);
    genealogie.setCommentaire(commentaire);
    genealogie.setTypeGenealogie(type);
    return genealogieEntiteAdminJpaDao.save(genealogie);
  }

  /**
   * Build a Commune with the given details.
   * @param codeInsee code INSEE for the Commune.
   * @param departement departement to which the Commune belongs.
   * @param debutValidite start date for the Communes validity.
   * @param tncc TypeNomClair of the Commune.
   * @param nomEnrichi the name of the Commune.
   * @param nomMajuscule the uppercase the name of the Commune.
   * @param commentaire comment about the Commune.
   * @return the newly built Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune buildCommune(final String codeInsee,
                               final String departement,
                               final Date debutValidite,
                               final String tncc,
                               final String nomEnrichi,
                               final String nomMajuscule,
                               final String commentaire)
    throws InvalidArgumentException {
    return buildCommune(codeInsee,
                        departement,
                        debutValidite,
                        metadataService.getTypeNomClair(tncc),
                        nomEnrichi,
                        nomMajuscule,
                        commentaire);
  }

  /**
   * Build a Commune with the given details.
   * @param codeInsee code INSEE for the Commune.
   * @param departement departement to which the Commune belongs.
   * @param debutValidite start date for the Communes validity.
   * @param tncc TypeNomClair of the Commune.
   * @param nomEnrichi the name of the Commune.
   * @param nomMajuscule the uppercase the name of the Commune.
   * @param commentaire comment about the Commune.
   * @return the newly built Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune buildCommune(final String codeInsee,
                               final String departement,
                               final Date debutValidite,
                               final TypeNomClair tncc,
                               final String nomEnrichi,
                               final String nomMajuscule,
                               final String commentaire)
    throws InvalidArgumentException {
    if (codeInsee == null || codeInsee.length() != 5) {
      throw new InvalidArgumentException("codeInsee is invalid: " + codeInsee);
    }
    if (departement == null || !codeInsee.startsWith(departement)) {
      throw new InvalidArgumentException(
              "departement is invalid: " + departement);
    }
    if (tncc == null || nomEnrichi == null) {
      throw new InvalidArgumentException(
              "The TypeNomClair and nomEnrichi are mandatory.");
    }
    Commune commune = new Commune();
    commune.setTypeEntiteAdmin(metadataService.getTypeEntiteAdmin("COM"));
    commune.setCodeInsee(codeInsee);
    commune.setDepartement(departement);
    commune.setDebutValidite(debutValidite);
    commune.setTypeNomClair(tncc);
    commune.setArticleEnrichi(tncc.getArticle());
    commune.setNomEnrichi(nomEnrichi);
    commune.setNomMajuscule(nomMajuscule == null ? StringConversionUtils.toUpperAscii(nomEnrichi)
                                                 : nomMajuscule);
    commune.setCommentaire(commentaire == null ? ""
                                               : commentaire);
    return commune;
  }

  /**
   * Invalidates the given Commune as of the given Date.
   * @param codeInsee code INSEE for the Commune.
   * @param dateEffective the date at which the Commune is no longer valid.
   * @return the newly invalidated Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  private Commune invalidateCommune(final String codeInsee, final Date dateEffective)
    throws InvalidArgumentException {
    if (codeInsee == null || dateEffective == null) {
      throw new InvalidArgumentException(
              "The codeInsee and date are mandatory.");
    }
    Commune commune = communeJpaDao.findByCodeInseeValidOnDate(codeInsee, dateEffective);
    if ((commune == null)) {
      throw new InvalidArgumentException(
              "There is no Commune with the given codeInsee (" + codeInsee
              + ") valid at the dateEffective ("+ dateEffective + ")");
    }
    if (commune.getFinValidite() != null) {
      throw new InvalidArgumentException(
              "The Commune has already been invalidated");
    }
    if (commune.getDebutValidite() != null
            && commune.getDebutValidite().after(dateEffective)) {
      throw new InvalidArgumentException(
              "The Commune is invalidated before first valid: "
              + commune.getDebutValidite());
    }
    commune.setFinValidite(dateEffective);
    return communeJpaDao.save(commune);
  }
}
