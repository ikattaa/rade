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
package fr.aesn.rade.webapp.mvc.referentiel;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.persist.model.CirconscriptionBassin;
import fr.aesn.rade.persist.model.Departement;
import fr.aesn.rade.persist.model.Region;
import fr.aesn.rade.service.BassinService;
import fr.aesn.rade.service.DepartementService;
import fr.aesn.rade.service.RegionService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring REST controller for AJAX queries.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
@RestController
@RequestMapping(JsonController.REQUEST_MAPPING)
public class JsonController {
  /** RequestMapping for this Controller. */
  public static final String REQUEST_MAPPING = "/referentiel/json";

  /** Region Service. */
  @Autowired @Setter
  private RegionService regionService;
  /** Departement Service. */
  @Autowired @Setter
  private DepartementService departementService;
  /** Bassin Service. */
  @Autowired @Setter
  private BassinService bassinService;

  /**
   * Returns a JSON list of Departements (code and name) for the given
   * parameters.
   * @param rawCode the INSEE code of the Region.
   * @param rawDate the date at which the returned data is valid.
   * @return JSON list of Departements (code and name).
   */
  @GetMapping("/deptlist")
  public @ResponseBody Map<String,String> getDepartementList(
          @RequestParam(value = "code", required = false) final String rawCode,
          @RequestParam(value = "date", required = false) final String rawDate) {
    Date date = decodeDate(rawDate);
    String code = rawCode == null || rawCode.isEmpty() || "-1".equals(rawCode)
                ? null
                : decodeString(rawCode);
    List<Departement> depts = departementService.getDepartementForRegion(code, date);
    HashMap<String, String> list = new HashMap<>();
    for(Departement dept : depts) {
      list.put(dept.getCodeInsee(), dept.getNomEnrichi());
    }
    return list;
  }

  /**
   * Returns a JSON list of Regions (code and name) for the given
   * parameters.
   * @param rawDate the date at which the returned data is valid.
   * @return JSON list of Regions (code and name).
   */
  @GetMapping("/regionlist")
  public @ResponseBody Map<String,String> getRegionList(
          @RequestParam(value = "date", required = false) final String rawDate) {
    Date date = decodeDate(rawDate);
    List<Region> regions = regionService.getAllRegion(date);
    HashMap<String, String> list = new HashMap<>();
    for(Region region : regions) {
      list.put(region.getCodeInsee(), region.getNomEnrichi());
    }
    return list;
  }

  /**
   * Returns a JSON list of Bassins (code and name) for the given
   * parameters.
   * @return JSON list of Bassins (code and name).
   */
  @GetMapping("/bassinlist")
  public @ResponseBody Map<String,String> getBassinList() {
    List<CirconscriptionBassin> bassins = bassinService.getAllBassin();
    HashMap<String, String> list = new HashMap<>();
    for(CirconscriptionBassin bassin : bassins) {
      list.put(bassin.getCode(), bassin.getLibelleLong());
    }
    return list;
  }

  /**
   * Check and decode the given String.
   * @param rawString the String to check and decode.
   * @return the decoded String.
   */
  private static final String decodeString(final String rawString) {
    if (rawString == null) {
      return null;
    }
    try {
      return URLDecoder.decode(rawString, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.info("Could not decode: {} - {}", rawString, e.getMessage());
      return rawString;
    }
  }

  /**
   * Check and parse the given date String. If null, return the current date.
   * @param rawDate the date String to check and parse.
   * @return the parsed date String.
   */
  private static final Date decodeDate(final String rawDate) {
    return DateConversionUtils.urlStringToDate(rawDate, new Date());
  }
}
