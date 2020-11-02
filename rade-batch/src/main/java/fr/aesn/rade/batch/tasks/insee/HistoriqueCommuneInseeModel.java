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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Model for an INSEE Commune History record.
 * 
 * Example file:
 * <code>
 * MOD DATE_EFF TYPECOM_AV COM_AV TNCC_AV         NCC_AV         NCCENR_AV        LIBELLE_AV        TYPECOM_AP COM_AP TNCC_AP NCC_AP NCCENR_AP LIBELLE_AP 
 * 32, 01/01/19,COM,01033,0,BELLEGARDE SUR VALSERINE,Bellegarde-sur-Valserine,Bellegarde-sur-Valserine,COM,01033,0,VALSERHONE,Valserhône,Valserhône
 * 32,01/01/19,COM,01033,0,BELLEGARDE SUR VALSERINE,Bellegarde-sur-Valserine,Bellegarde-sur-Valserine,COMD,01033,0,BELLEGARDE SUR VALSERINE,Bellegarde-sur-Valserine,Bellegarde-sur-Valserine
 * 32,01/01/19,COM,01091,0,CHATILLON EN MICHAILLE,Châtillon-en-Michaille,Châtillon-en-Michaille,COM,01033,0,VALSERHONE,Valserhône,Valserhône
 * 32,01/01/19,COM,01205,0,LANCRANS,Lancrans,Lancrans,COM,01033,0,VALSERHONE,Valserhône,Valserhône
 * 32,01/01/19,COM,01036,0,BELMONT LUTHEZIEU,Belmont-Luthézieu,Belmont-Luthézieu,COM,01036,0,VALROMEY SUR SERAN,Valromey-sur-Séran,Valromey-sur-Séran
 * 32,01/01/19,COM,01036,0,BELMONT LUTHEZIEU,Belmont-Luthézieu,Belmont-Luthézieu,COMD,01036,0,BELMONT LUTHEZIEU,Belmont-Luthézieu,Belmont-Luthézieu
 * 32,01/01/19,COM,01221,0,LOMPNIEU,Lompnieu,Lompnieu,COM,01036,0,VALROMEY SUR SERAN,Valromey-sur-Séran,Valromey-sur-Séran
 * 32,01/01/19,COM,01414,0,SUTRIEU,Sutrieu,Sutrieu,COM,01036,0,VALROMEY SUR SERAN,Valromey-sur-Séran,Valromey-sur-Séran
 * 32,01/01/19,COM,01442,0,VIEU,Vieu,Vieu,COM,01036,0,VALROMEY SUR SERAN,Valromey-sur-Séran,Valromey-sur-Séran
 * </code>
 * Columns Description:
 * <table>
 * <tr><th>Longueur</th>  <th>Nom</th>    <th>Désignation en clair</th></tr>
 * <tr><td>2</td>         <td>MOD</td>    <td>Type d'événement de communes</td></tr>
 * <tr><td>10</td>         <td>DATE_EFF</td>     <td>Date d'effet (AAA-MM-JJ)</td></tr>
 * <tr><td>4</td>         <td>TYPECOM_AV</td>     <td>Type de la commune avant événement</td></tr>
 * <tr><td>5</td>         <td>COM_AV</td>    <td>Code de la commune avant événement</td></tr>
 * <tr><td>1</td>         <td>TNCC_AV</td>    <td>Type de nom en clair</td></tr>
 * <tr><td>200</td><td>NCC_AV</td>     <td>Nom en clair (majuscules)</td></tr>
 * <tr><td>200</td>       <td>NCCENR_AV</td>    <td>Nom en clair (typographie riche)</td></tr>
 * <tr><td>200</td>       <td>LIBELLE_AV</td>    <td>Nom en clair (typographie riche) avec article</td></tr>
 * <tr><td>4</td>         <td>TYPECOM_AP</td>    <td>Type de commune après l'événement</td></tr>
 * <tr><td>5</td>         <td>COM_AP</td> <td>Code de la commune après l'événement</td></tr>
 * <tr><td>1</td>         <td>TNCC_AP</td> <td>Type de nom en clair</td></tr>
 * <tr><td>200</td>       <td>NCC_AP</td>  <td>Nom en clair (majuscules)</td></tr>
 * <tr><td>200</td>       <td>NCCENR_AP</td>Nom en clair (typographie riche)<td></td></tr>
 * <tr><td>200</td>       <td>LIBELLE_AP</td> <td>Nom en clair (typographie riche) avec article</td></tr>
 * </table>
 * 
 * For more details, see
 * https://www.insee.fr/fr/information/3363419
 * 
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Getter @Setter @NoArgsConstructor
@ToString @EqualsAndHashCode
public class HistoriqueCommuneInseeModel implements Serializable {
  /** Unique Identifier for Serializable Class. */
  private static final long serialVersionUID = 332832229694762679L;

  /** MOD - Type d'événement de communes. */
  @Size(max = 2)
  private String typeEvenCommune;

  /** DATE_EFF - Date d'effet (AAA-MM-JJ). */
  @NotNull
  private Date dateEffet;

  /** TYPECOM_AV - Type de la commune avant événement. */
  @Size(max = 4)
  private String typeCommuneAvantEven;

  /** COM_AV - Code de la commune avant événement. */
  @Size(max = 5)
  private String codeCommuneAvantEven;

  /** TNCC_AV - Type de nom en clair. */
  @Size(max = 1)
  private String typeNomClairAv;

  /** NCC_AV - Nom en clair (majuscules). */
  @Size(max=200)
  private String nomClairMajAv;

  /** NCCENR_AV - Nom en clair (typographie riche). */
  @Size(max=200)
  private String nomClairTypographieRicheAv;

  /** LIBELLE_AV - Nom en clair (typographie riche) avec article. */
  @Size(max = 200)
  private String nomClairTypographieRicheAvecArticleAv;

  /** TYPECOM_AP - Type de commune après l'événement. */
  @Size(max = 4)
  private String typeCommuneAprEven;

  /** COM_AP - Code de la commune après l'événement. */
  @Size(max = 5)
  private String codeCommuneaprEven;

  /** TNCC_AP - Type de nom en clair. */
  @Size(max=1)
  private String typeNomClairAp;

  /** NCC_AP - Nom en clair (majuscules). */
  @Size(max=200)
  private String nomClairMajAp;

  /** NCCENR_AP - Nom en clair (typographie riche). */
  @Size(max = 200)
  private String nomClairTypographieRicheAp;

  /** LIBELLE_AP - Nom en clair (typographie riche) avec article. */
  @Size(max = 200)
  private String  nomClairTypographieRicheAvecArticleAp;

  /**
   * Pair of INSEE Commune History records that are associated.
   * @author Marc Gimpel (mgimpel@gmail.com)
   */
  @Getter
  public static class Pair {
    /** Parent INSEE Commune History record. */
    private HistoriqueCommuneInseeModel parent;
    /** Child INSEE Commune History record. */
    private HistoriqueCommuneInseeModel enfant;

    /**
     * Full Constructor.
     * @param parent Parent INSEE Commune History record.
     * @param enfant Child INSEE Commune History record.
     */
    public Pair(HistoriqueCommuneInseeModel parent,
                HistoriqueCommuneInseeModel enfant) {
      this.parent = parent;
      this.enfant = enfant;
    }

    
    /** TODO : a voir ou cette fonction est utilisé.
     * Pair is considered valid if the effective date of the change is the same
     * and the Commune d'Echange (COMECH) field of one corresponds to the Code
     * INSEE Commune of the other.
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
    	return true;
//      return (parent != null && enfant != null
//        && parent.getCommuneEchange() != null
//        && enfant.getCommuneEchange() != null
//        && parent.getDateEffet().equals(enfant.getDateEffet())
//        && parent.getCommuneEchange().equals(enfant.getCodeDepartement()
//                                           + enfant.getCodeCommune())
//        && enfant.getCommuneEchange().equals(parent.getCodeDepartement()
//                                           + parent.getCodeCommune()));
    }

    /**
     * Returns the effective date of the Pair, or null if the Pair isn't valid.
     * @return the effective date of the Pair, or null if the Pair isn't valid.
     */
    public Date getDateEffet() {
      return isValid() ? parent.getDateEffet() : null;
    }
  }

  /**
   * Set of INSEE Commune History Pairs that are associated into one Changeset.
   * @author Marc Gimpel (mgimpel@gmail.com)
   */
  @Getter
  public static class Changeset {
    /** Set of INSEE Commune History Pairs that make up the Changeset. */
    private List<Pair> pairs;

    /**
     * Basic Constructor.
     */
    public Changeset() {
      pairs = new ArrayList<>();
    }

    /**
     * Append all of the Pairs in the given collection to the end of the list.
     * @param collection Pairs to be appended to the end of the list.
     */
    public void addAll(Collection<Pair> collection) {
      pairs.addAll(collection);
    }

    /**
     * Changeset is considered valid if all the Pairs are associated, in
     * particular if they all have the same effective date, and their number
     * all correspond to their NBCOM field.
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
      if (pairs.isEmpty()) {
        return false;
      }
      Date eff = pairs.get(0).getDateEffet();
      /* TODO vérifier si il faut remplacer nbcom :
       * Integer nbcom = pairs.get(0).getParent().getNombreCommunes();
      if (eff == null || nbcom == null) { */
      if (eff == null){
        return false;
      }
      /* TODO vérifier si il faut remplacer nbcom :
       * if (pairs.size() != nbcom
              && */
      if (!"Roche-sur-Yon".equals(pairs.get(0).getParent().getNomClairTypographieRicheAp())) {
        // Le 25/08/1964 Saint-André-d'Ornay (85195) et Bourg-sous-la-Roche-sur-Yon (85032)
        // ont fusionné avec Roche-sur-Yon (85191) mais tous les deux sont de rang=1 & nb=1
        return false;
      }
      for (Pair pair : pairs) {
        if (!pair.isValid()
          || !eff.equals(pair.getDateEffet())) {
          return false;
        }
      }
      return true;
    }

    /**
     * Returns the effective date of the Set, or null if the Set isn't valid.
     * @return the effective date of the Set, or null if the Set isn't valid.
     */
    public Date getDateEffet() {
      return isValid() ? pairs.get(0).getDateEffet() : null;
    }
  }
}
