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
package fr.aesn.rade.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.aesn.rade.common.InvalidArgumentException;
import fr.aesn.rade.persist.model.Audit;
import fr.aesn.rade.persist.model.Commune;

/**
 * Service Interface for Commune.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
public interface CommuneService {
  /**
   * List all Commune.
   * @return a List of all the Commune.
   */
  public List<Commune> getAllCommune();

  /**
   * List all Commune valid at the given date.
   * @param date the date at which the code was valid
   * @return a List of all the Commune.
   */
  public List<Commune> getAllCommune(Date date);

  /**
   * Returns a List of all Commune from the given departement, resembling the
   * given name and valid at the given date.
   * @param dept the departement of the Communes.
   * @param nameLike a pattern to search for Communes with a name resembling.
   * @param date the date at which the Communes were valid.
   * @return a List of all Commune matching the given parameters.
   */
  public List<Commune> getAllCommune(String dept, String nameLike, Date date);

  /**
   * Returns a Map of all Commune indexed by ID.
   * @return a Map of all Commune indexed by ID.
   */
  public Map<Integer, Commune> getCommuneMap();

  /**
   * Returns a Map of all Commune valid at the given date and indexed by code.
   * @param date the date at which the Commune are valid.
   * @return a Map of all Commune indexed by code INSEE.
   */
  public Map<String, Commune> getCommuneMap(Date date);

  /**
   * Get the Commune with the given ID.
   * @param id the Commune ID.
   * @return the Commune with the given ID.
   */
  public Commune getCommuneById(int id);

  /**
   * Get the Commune with the given code.
   * @param code the Commune code.
   * @return list of Communes that have historically had the given code.
   */
  public List<Commune> getCommuneByCode(String code);

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  public Commune getCommuneByCode(String code, Date date);
  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  public Commune getCommuneActiveByCode(String code, Date date);
  public List<Commune> getAllCommuneEnfantActiveByCodeInactiveParent(String code, Date date);

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  public Commune getCommuneByCode(String code, String date);

  /**
   * Invalidates the given commune by setting the communes finValidite
   * field to the given date.
   * @param commune the commune to invalidate.
   * @param date the date of end of validity for the commune.
   * @return the now invalidated commune.
   */
  public Commune invalidateCommune(Commune commune, Date date);

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
  public Commune mod10ChangementdeNom(Date dateEffective, Audit audit,
                                       String codeInsee, String tnccoff,String nomMajuscule,
                                       String nccoff, String commentaire)
    throws InvalidArgumentException;

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
  public Commune mod20Creation(Date dateEffective, Audit audit,
                                String codeInsee, String departement,
                                String tnccoff, String nccoff,String nomMajuscule,
                                String commentaire)
    throws InvalidArgumentException;

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
  public void mod21Retablissement(Date dateEffective, Audit audit,
                                       Commune com21retabli,
                                       Commune com21source,
                                       String commentaire)
    throws InvalidArgumentException;

  /**
   * Creates (MOD=30 : Suppression)
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the code of the commune.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  public Commune mod30Supression(Date dateEffective, Audit audit,
                                String codeInsee)
    throws InvalidArgumentException;

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
  public Commune mod31x32Fusion(Date dateEffective, Audit audit,
                                  List<Commune> com31absorbe,
                                  Commune com32absorbant,
                                  String commentaire)
    throws InvalidArgumentException;

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
  public Commune mod33x34FusionAssociation(Date dateEffective, Audit audit,
                                             List<Commune> com33associe,
                                             Commune com34absorbant,
                                             String commentaire)
    throws InvalidArgumentException;

  /**
   * Changes the departement/code (MOD=41 : Changement de departement,
   * MOD=50 : Changement de code) 
   * @param dateEffective the date that the change takes effect.
   * @param audit audit details about change.
   * @param codeInsee the new code of the Commune.
   * @param departement the new departement to which the Commune belongs.
   * @param oldCodeInsee the old code for the Commune.
   * @param commentaire comment for the genealogie link.
   * @param mod modification code.
   * @return the new Commune.
   * @throws InvalidArgumentException if an invalid argument has been passed.
   */
  public Commune mod41x50ChangementCodeCom(Date dateEffective, Audit audit,
                                      String codeInsee, String departement,
                                      String oldCodeInsee,String nomMajuscule, String commentaire, String mod)
    throws InvalidArgumentException;


}
