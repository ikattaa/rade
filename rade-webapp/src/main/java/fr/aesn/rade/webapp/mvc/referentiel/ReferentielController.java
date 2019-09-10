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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.aesn.rade.persist.model.Region;
import fr.aesn.rade.service.RegionService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC Controller for Rade.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
@Controller
@RequestMapping(ReferentielController.REQUEST_MAPPING)
public class ReferentielController {
  /** Service. */
  @Autowired @Setter
  private RegionService regionService;
  /** RequestMapping for this Controller. */
  public static final String REQUEST_MAPPING = "/referentiel";

  public static final String REGION  = "region";
  public static final String DEPT    = "dept";
  public static final String COMMUNE = "commune";
  public static final String BASSIN  = "bassin"; 

  /**
   * Entite search mapping
   * Recherche d'entité (commune, région, département, bassin, délégation) par
   * code.
   * @param model
   * @param entite
   * @return redirect to the suitable entity search
   */
  @RequestMapping("/entiteSearch")
  public String entiteSearch(final Model model,
                             @ModelAttribute final ReferentielSearchModel entite) {
    log.info("Recherche d'entités, type : {}, code :{}",
             entite.getType(), entite.getCode());
    if(entite.getCode() != null && !entite.getCode().isEmpty()) {
      // une fois la combo des types dégrisée, décommenter la ligne suivante
      String type = COMMUNE; // entite.getType();
      if(COMMUNE.equalsIgnoreCase(type)) {
        return "redirect:/referentiel/commune/" + entite.getCode();
      }
    }
    return "home";
  }

  /**
   * Region Search mapping.
   * @param model MVC model passed to JSP.
   * @param code INSEE code for Region.
   * @return View for the page.
   */
  @RequestMapping(value = "/region", method = RequestMethod.GET)
  public String regionsearch(final Model model,
                             @RequestParam(value = "code", required = false) final String code) {
    log.debug("Search for region: {}", code);
    if (code != null) {
      Region region = regionService.getRegionByCode(code, new Date());
      return (regiondisplay(model, region));
    }
    model.addAttribute("titre", "Recherche Region");
    model.addAttribute("region", new Region());
    return "referentiel/regionsearch";
  }

  /**
   * Region Search mapping.
   * @param model MVC model passed to JSP.
   * @param result binding to Region object result.
   * @param criteria Region search criteria entered in form.
   * @return View for the page.
   */
  @RequestMapping(value = "/region", method = RequestMethod.POST)
  public String regiondisplay(final Model model,
                              final BindingResult result,
                              @ModelAttribute("region") final Region criteria) {
    log.debug("Search for region with criteria: {}", criteria);
    if (result.hasErrors()) {
      return "error";
    }
    Region region = regionService.getRegionByCode(criteria.getCodeInsee(), new Date());
    return (regiondisplay(model, region));
  }

  /**
   * Region Search mapping.
   * @param model MVC model passed to JSP.
   * @param code INSEE code for Region.
   * @return View for the page.
   */
  @RequestMapping(value = "/region/{code}")
  public String regiondisplay(final Model model,
                              @PathVariable("code") final String code) {
    log.debug("Display region: {}", code);
    if (code != null) {
      Region region = regionService.getRegionByCode(code, new Date());
      if (region != null) {
        return (regiondisplay(model, region));
      }
    }
    return "redirect:/referentiel/region";
  }

  /**
   * @param region the region to display.
   * @param model the Spring MVC model.
   * @return View for the region display page.
   */
  private String regiondisplay(final Model model,
                               final Region region) {
    model.addAttribute("titre", "Region");
    model.addAttribute("region", region);
    return "referentiel/regiondisplay";
  }

  /**
   * Departement Search mapping.
   * @param model MVC model passed to JSP.
   * @param code INSEE code for Departement.
   * @return View for the page.
   */
  @RequestMapping(value = "/departement", method = RequestMethod.GET)
  public String departementsearch(final Model model,
                                  @RequestParam(value = "code", required = false) final String code) {
    log.debug("Search for departement: {}", code);
    model.addAttribute("titre", "Recherche Département");
    return "todo";
  }

  /**
   * Bassin Search mapping.
   * @param model MVC model passed to JSP.
   * @param code Sandre code for Bassin.
   * @return View for the page.
   */
  @RequestMapping(value = "/bassin", method = RequestMethod.GET)
  public String bassinsearch(final Model model,
                             @RequestParam(value = "code", required = false) final String code) {
    log.debug("Search for bassin: {}", code);
    model.addAttribute("titre", "Recherche Bassin");
    return "todo";
  }

  /**
   * Delegation Search mapping.
   * @param model MVC model passed to JSP.
   * @param code Delegation code.
   * @return View for the page.
   */
  @RequestMapping(value = "/delegation", method = RequestMethod.GET)
  public String delegationsearch(final Model model,
                                 @RequestParam(value = "code", required = false) final String code) {
    log.debug("Search for delegation: {}", code);
    model.addAttribute("titre", "Recherche Délégation");
    return "todo";
  }
}
