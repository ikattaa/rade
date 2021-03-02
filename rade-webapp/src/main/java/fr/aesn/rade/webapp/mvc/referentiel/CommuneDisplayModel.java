/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Sophie Belin
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
package fr.aesn.rade.webapp.mvc.referentiel;

import fr.aesn.rade.common.modelplus.CommunePlus;
import fr.aesn.rade.common.modelplus.CommunePlusWithGenealogie;
import fr.aesn.rade.common.modelplus.GenealogieSimple;
import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.persist.model.Departement;
import fr.aesn.rade.persist.model.Region;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/**
 * Affichage d'une commune.
 * @author sophie.belin
 */
@ToString @Getter
public class CommuneDisplayModel {
  private String codeInsee;
  private String motifModification;
  private String nomEnrichi;
  private String commentaireModification;
  private String article;
  private String articleEnrichi;
  private String codeBassin;
  private String nomBassin;
  private String codeDepartement;
  private String nomDepartement;
  private String nomMajuscule;
  private String nomRegion;
  private Date debutValidite;
  private Date finValidite;
  private Map<String, GenealogieSimple> parents;
  private Map<String, GenealogieSimple> enfants;

  /**
   * Constructor.
   * @param communePlusWithGenealogie Commune details.
   */
  public CommuneDisplayModel(final CommunePlusWithGenealogie communePlusWithGenealogie) {
    CommunePlus commune = communePlusWithGenealogie.getCommunePlus();
    this.codeInsee = commune.getCodeInsee();
   this.nomMajuscule = commune.getNomMajuscule();
    this.nomEnrichi = commune.getNomEnrichi();
    this.debutValidite = commune.getDebutValiditeCommuneInsee();
    this.finValidite = commune.getFinValiditeCommuneInsee();
    this.article = commune.getTypeNomClair().getArticle();
    this.articleEnrichi = commune.getArticleEnrichi();
    this.parents = communePlusWithGenealogie.getParents();
    this.enfants = communePlusWithGenealogie.getEnfants();
    Map<String, GenealogieSimple> genealogie = null;
    if(!parents.isEmpty()) {
      genealogie = communePlusWithGenealogie.getParents();
    } else if(!enfants.isEmpty()) {
      genealogie = communePlusWithGenealogie.getEnfants();
    }
    if (genealogie != null) {
      Iterator<Map.Entry<String, GenealogieSimple>> it = genealogie.entrySet().iterator();
      if(it.hasNext()){
        this.motifModification = (it.next().getValue()).getTypeModification().getLibelleLong();
      }
    }
    if(communePlusWithGenealogie.getCommunePlus().getCirconscriptionBassin() != null){
      this.nomBassin = communePlusWithGenealogie.getCommunePlus().getCirconscriptionBassin().getLibelleLong();
      this.codeBassin = communePlusWithGenealogie.getCommunePlus().getCirconscriptionBassin().getCode();
    }
  }

  /**
   * Constructor.
   * @param communePlusWithGenealogie Commune details.
   * @param departement Departement details.
   * @param region Region details.
   */
  public CommuneDisplayModel(final CommunePlusWithGenealogie communePlusWithGenealogie,
                             final Departement departement,
                             final Region region) {
    this(communePlusWithGenealogie);
    this.nomDepartement = departement.getNomEnrichi();
    this.codeDepartement = departement.getCodeInsee();
    this.nomRegion = region.getNomEnrichi();
  }

  /**
   * Returns the URL to display the Entity.
   * @param appContext application Context
   * @return the URL to display the Entity.
   */
  public String getUrlEntite(final String appContext) {
    return getUrlEntite(appContext, codeInsee, finValidite);
  }

  /**
   * Returns the URL to display the Entity.
   * @param appContext application Context
   * @param codeInsee the code INSEE of the Entity
   * @param dateFinValidite 
   * @return the URL to display the Entity.
   */
  public static String getUrlEntite(final String appContext,
                                    final String codeInsee,
                                    final Date dateFinValidite) {
    if(codeInsee == null) {
      return null;
    }
    String url = appContext + CommuneController.REQUEST_MAPPING + "/" + codeInsee;
    if (dateFinValidite == null) {
      return url;
    } else {
      Date date = new Date(dateFinValidite.getTime() - 1);
      return url + "?date=" + DateConversionUtils.toUrlString(date);
    }
  }
}
