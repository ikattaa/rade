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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.aesn.rade.common.InvalidArgumentException;
import fr.aesn.rade.common.modelplus.CommunePlus;
import fr.aesn.rade.common.modelplus.CommunePlusWithGenealogie;
import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.persist.dao.CommuneJpaDao;
import fr.aesn.rade.persist.dao.CommuneSandreJpaDao;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.CommuneSandre;
import fr.aesn.rade.persist.model.EntiteAdministrative;
import fr.aesn.rade.persist.model.GenealogieEntiteAdmin;
import fr.aesn.rade.service.CommunePlusService;
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
public class CommunePlusServiceImpl
  implements CommunePlusService {
  /** Data Access Object for Commune. */
  @Autowired @Setter
  private CommuneJpaDao communeJpaDao;
  /** Data Access Object for CommuneSandre. */
  @Autowired @Setter
  private CommuneSandreJpaDao communeSandreJpaDao;

  /**
   * List all Commune valid at the given date.
   * @param date the date at which the code was valid
   * @return a List of all the Commune.
   */
  @Override
  @Transactional(readOnly = true)
  public List<CommunePlus> getAllCommuneValidOnDate(final Date date) {
    log.debug("Commune list requested for Date: date={}", date);
    Date testdate = (date == null ? new Date() : date);
    List<Commune> listInsee = communeJpaDao.findAllValidOnDate(testdate);
    List<CommuneSandre> listSandre = communeSandreJpaDao.findAllValidOnDate(testdate);
    HashMap<String, CommuneSandre> map = new HashMap<>(listSandre.size());
    for (CommuneSandre item : listSandre) {
      map.put(item.getCodeCommune(), item);
    }
    String code;
    CommunePlus commune;
    CommuneSandre sandre;
    List<CommunePlus> result = new ArrayList<>(listInsee.size());
    for (Commune item : listInsee) {
      if (item != null) {
        code = item.getCodeInsee();
        try {
          commune = new CommunePlus(code, testdate);
          commune.setCommuneInsee(item);
          sandre = map.get(code);
          if (sandre != null) {
            commune.setCommuneSandre(sandre);
          }
          result.add(commune);
        }
        catch (InvalidArgumentException e) {
          log.warn("Error assembling CommunePlus for Commune {} on {}", code, testdate, e);
        }
      }
    }
    return result;
  }

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  @Override
  @Transactional(readOnly = true)
  public CommunePlus getCommuneByCodeValidOnDate(final String code, final Date date) {
    log.debug("Commune requested by code and date: code={}, date={}", code, date);
    if (code == null) {
      return null;
    }
    Date testdate = (date == null ? new Date() : date);
    Commune insee = communeJpaDao.findByCodeInseeValidOnDate(code, testdate);
    if (insee == null) {
      return null;
    }
    CommuneSandre sandre = communeSandreJpaDao.findByCodeInseeValidOnDate(code, testdate);
    CommunePlus result = new CommunePlus(code, testdate);
    try {
      result.setCommuneInsee(insee);
      if (sandre != null) {
        result.setCommuneSandre(sandre);
      }
    }
    catch (InvalidArgumentException e) {
      log.warn("Error assembling CommunePlus for Commune {} on {}", code, testdate, e);
    }
    return result;
  }

  /**
   * Get the Commune with the given code at the given date.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date.
   */
  @Override
  @Transactional(readOnly = true)
  public CommunePlus getCommuneByCodeValidOnDate(final String code, final String date) {
    log.debug("Commune requested by code and date: code={}, date={}", code, date);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return getCommuneByCodeValidOnDate(code, sdf.parse(date));
    } catch (ParseException e) {
      log.warn("Commune requested by code and date: Exception parsing date {}", date, e);
      return null;
    }
  }
  
  /**
   * Returns a List of all Commune from the given codeInsee, departement,
   * region, circonscription, commune name and/or date.
   * @param code the code of the Communes.
   * @param dept the departement of the Communes.
   * @param region the region of the Communes.
   * @param bassin the circonscription of the Communes.
   * @param nameLike a pattern to search for Communes with a name resembling.
   * @param date the date at which the Communes were valid.
   * @return a List of all Commune matching the given parameters.
   */
  @Override
  @Transactional(readOnly = true)
  public List<CommunePlusWithGenealogie> getCommuneByCriteria(final String code,
                                                              final String dept,
                                                              final String region,
                                                              final String bassin,
                                                              final String nameLike,
                                                              final Date date) {
    log.debug("Commune with Genealogie requested with criteria: " +
              "code={}, dept={}, region={}, bassin={}, name={}, date={}",
              code, dept, region, bassin, nameLike, date);
    List<CommunePlusWithGenealogie> communesPlus = new ArrayList<>();
    if(code != null && !code.isEmpty() && date != null) { // Rechercher par code INSEE (ignorer dept, region, ...)
      CommunePlusWithGenealogie communeGenealogie = getCommuneValidOnDateWithGenealogie(code, date);
      if(communeGenealogie != null) {
        communesPlus.add(communeGenealogie);
      }
      return communesPlus;
    }
    // Rechercher les communes en fonction du code quand la date est null
    List<Commune> communes = null;
    String testname = (nameLike == null || nameLike.isEmpty() ? "%" : "%" + nameLike + "%");
    if (code != null  && date == null) {
      // code is given and date is null
      communes =   communeJpaDao.findByCodeInsee(code);
    }
    // Rechercher les communes en fonction du nom, dept, region
    else if ((dept == null || dept.isEmpty()) && (region == null || region.isEmpty())) {
      // neither region or departement are given
      communes = (date == null ?
              communeJpaDao.findByDepartementLikeAndNomEnrichiLikeIgnoreCaseOrderByNomEnrichiAsc("%", testname) :
              communeJpaDao.findByDepartementLikeAndNomEnrichiLikeIgnoreCaseValidOnDate("%", testname, date));
    } else if ((dept == null || dept.isEmpty()) && (region != null && !region.isEmpty())) {
      // only region is given
      communes = (date == null ?
              communeJpaDao.findByRegionLikeAndNomEnrichiLikeIgnoreCase(region, testname) :
              communeJpaDao.findByRegionLikeAndNomEnrichiLikeIgnoreCaseValidOnDate(region, testname, date));
    } else {
      // department is given
      communes = (date == null ?
              communeJpaDao.findByDepartementLikeAndNomEnrichiLikeIgnoreCaseOrderByNomEnrichiAsc(dept, testname) :
              communeJpaDao.findByDepartementLikeAndNomEnrichiLikeIgnoreCaseValidOnDate(dept, testname, date));
    }
    // Filter les communes en fonction du bassin et récupérer le genealogie
    CommuneSandre sandre;
    CommunePlus communePlus;
    Date testdate;
    for(Commune commune : communes) {
      if (date != null) {
        testdate = date;
      } else {
        // date non défini donc prendre la fin de validité de la commune
        // (ou la date courante si la commune est toujours valide)
        testdate = commune.getFinValidite();
        testdate = (testdate == null ? new Date()
                                     : Date.from(DateConversionUtils.toZonedDateTime(testdate, null).minusDays(1).toInstant()));
      }
      sandre = communeSandreJpaDao.findByCodeInseeValidOnDate(commune.getCodeInsee(), testdate);
      communePlus = new CommunePlus(commune.getCodeInsee(), testdate);
      try {
        communePlus.setCommuneInsee(commune);
        if (sandre != null) {
          communePlus.setCommuneSandre(sandre);
        }
      }
      catch (InvalidArgumentException e) {
        log.warn("Error assembling CommunePlus for Commune {} on {}", code, testdate, e);
      }
      if (bassin == null || bassin.isEmpty() || (sandre != null && bassin.equals(sandre.getCirconscriptionBassin().getCode()))) {
        communesPlus.add(buildCommuneWithGenealogie(communePlus));
      }
    }
    return communesPlus;
  }

  /**
   * Get the Commune with the given code at the given date, and all it's
   * genealogie.
   * @param code the Commune code.
   * @param date the date at which the code was valid
   * @return the Commune with the given code at the given date, and all it's
   * genealogie.
   */
  @Override
  @Transactional(readOnly = true)
  public CommunePlusWithGenealogie getCommuneValidOnDateWithGenealogie(final String code,
                                                            final Date date) {
    CommunePlus commune = getCommuneByCodeValidOnDate(code, date);
    if (commune == null) {
      return null;
    }
    return buildCommuneWithGenealogie(commune);
  }

  /**
   * Build a CommunePlusWithGenealogie from the given CommunePlus.
   * @param commune the CommunePlus
   * @return a CommunePlusWithGenealogie built from the given CommunePlus.
   */
  private CommunePlusWithGenealogie buildCommuneWithGenealogie(final CommunePlus commune) {
    log.debug("Building Genealogie for {}", commune);
    CommunePlusWithGenealogie result = new CommunePlusWithGenealogie(commune);
    EntiteAdministrative tempEntity;
    Optional<Commune> opt;
    Set<GenealogieEntiteAdmin> parents = commune.getParentsInsee();
    if (parents != null) {
      for (GenealogieEntiteAdmin parent : parents) {
        tempEntity = parent.getParentEnfant().getParent();
        assert "COM".equals(tempEntity.getTypeEntiteAdmin().getCode());
        opt = communeJpaDao.findById(tempEntity.getId());
        if (opt.isPresent()) {
          try {
            result.addParent(parent.getTypeGenealogie(), opt.get());
          } catch (InvalidArgumentException e) {
            log.warn("This should never happen! parent must exist ({}): {}", e.getMessage(), parent);
          }
        } else {
          log.warn("This should never happen! parent must exist: {}", parent);
        }
      }
    }
    Set<GenealogieEntiteAdmin> enfants = commune.getEnfantsInsee();
    if (enfants != null) {
      for (GenealogieEntiteAdmin enfant : enfants) {
        tempEntity = enfant.getParentEnfant().getEnfant();
        assert "COM".equals(tempEntity.getTypeEntiteAdmin().getCode());
        opt = communeJpaDao.findById(tempEntity.getId());
        if (opt.isPresent()) {
          try {
            result.addEnfant(enfant.getTypeGenealogie(), opt.get());
          } catch (InvalidArgumentException e) {
            log.warn("This should never happen! child must exist ({}): {}", e.getMessage(), enfant);
          }
        } else {
          log.warn("This should never happen! child must exist: {}", enfant);
        }
      }
    }
    return result;
  }
}
