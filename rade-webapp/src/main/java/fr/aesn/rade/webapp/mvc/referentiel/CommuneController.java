/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2019 Sophie Belin
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

import fr.aesn.rade.common.modelplus.CommunePlusWithGenealogie;
import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.persist.model.Departement;
import fr.aesn.rade.persist.model.Region;
import fr.aesn.rade.service.CommunePlusService;
import fr.aesn.rade.service.DepartementService;
import fr.aesn.rade.service.RegionService;
import fr.aesn.rade.webapp.export.Export;
import fr.aesn.rade.webapp.export.ExportExcel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Spring MVC controller for searching and displaying communes.
 * @author sophie.belin
 */
@Slf4j
@Controller
@RequestMapping(CommuneController.REQUEST_MAPPING)
@SessionAttributes(CommuneController.COMMUNE_SEARCH_MODEL)
public class CommuneController {
  /** RequestMapping for this Controller. */
  public static final String REQUEST_MAPPING = "/referentiel/commune";
  /** Session Attribute of the Commune Search Model. */
  public static final String COMMUNE_SEARCH_MODEL = "communeSearchModel";
  /** Session Attribute of the Commune Display Model. */
  public static final String COMMUNE_DISPLAY_MODEL = "communeDisplay";
  
  /** Default name for the export file. */
  public static final String DEFAULT_EXPORT_FILENAME = "export-communes";

  /** I18n message source. */
  @Autowired @Setter 
  private MessageSource messageSource;
  /** Commune Service. */
  @Autowired @Setter 
  private CommunePlusService communePlusService;
  /** Region Service. */
  @Autowired @Setter 
  private RegionService regionService;
  /** Departement Service. */
  @Autowired @Setter 
  private DepartementService departementService;

  /**
   * Session Attribute Bean for Search Parameters and Result.
   * @return Session Attribute Bean for Search Parameters and Result.
   */
  @ModelAttribute(COMMUNE_SEARCH_MODEL)
  private CommuneSearchModel searchCommune() {
    return new CommuneSearchModel();
  }

  /**
   * Get the Search Form. 
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return the corresponding view.
   */
  @GetMapping()
  public String getSearchForm(final Locale locale,
                              final Model model,
                              @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    return viewCommuneSearch(locale, model, communeSearchModel);
  }

  /**
   * Reset the search form.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return redirect back to the search form.
   */
  @PostMapping(params = "annuler", value = "/resultats")
  public String resetSearchForm(final Locale locale,
                                final Model model,
                                @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    communeSearchModel.reset();
    return "redirect:/referentiel/commune";
  }

  /**
   * Submit (POST) the search form.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return the corresponding view or redirect appropriately.
   */
  @PostMapping(params = "valider", value = "/resultats")
  public String submitSearchForm(final Locale locale,
		                         final Model model,
                                 @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    log.debug("Search for commune with criteria: {}", communeSearchModel);
    String dept   = "-1".equals(communeSearchModel.getCodeDepartement())
                   ? null : communeSearchModel.getCodeDepartement();
    String region = "-1".equals(communeSearchModel.getCodeRegion())
                   ? null : communeSearchModel.getCodeRegion();
    String bassin = "-1".equals(communeSearchModel.getCodeCirconscription())
                   ? null : communeSearchModel.getCodeCirconscription();
    if( StringUtils.isEmpty(communeSearchModel.getCodeInsee())
        && dept == null && region == null && bassin == null
        && StringUtils.isEmpty(communeSearchModel.getNomEnrichi())) {
      model.addAttribute("errorMessage", messageSource.getMessage("communesearch.error.empty", null, locale));
      return viewCommuneSearch(locale, model, communeSearchModel);
    }
    List<CommunePlusWithGenealogie> communes = null;
    communes = communePlusService.getCommuneByCriteria(communeSearchModel.getCodeInsee(),
                                          dept,
                                          region,
                                          bassin,
                                          communeSearchModel.getNomEnrichi(),
                                          communeSearchModel.getDateEffet());
    if(communes == null || communes.isEmpty()){
      model.addAttribute("errorMessage", messageSource.getMessage("communesearch.error.noresult", null, locale));
      return viewCommuneSearch(locale, model, communeSearchModel);
    }
    communeSearchModel.setCommunes(communes);
    communeSearchModel.setPage(1);
    if(communes.size() == 1) {
      CommunePlusWithGenealogie commune = communes.get(0);
      Date date = communeSearchModel.getDateEffet();
      return "redirect:/referentiel/commune/"
             + commune.getCommunePlus().getCodeInsee()
             + (date == null ? "" : "?date=" + DateConversionUtils.toUrlString(date));
    } else {
      return "redirect:/referentiel/commune/resultats?page=1";
    }
  }

  /**
   * Get the search result list.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return the corresponding view or redirect appropriately.
   */
  @GetMapping("/resultats")
  public String getResultList(final Locale locale,
                              final Model model,
                              @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    if(communeSearchModel.getCommunes() != null && communeSearchModel.getCommunes().size() > 1) {
      return viewCommuneResults(locale, model, communeSearchModel);
    } else {
      return "redirect:/referentiel/commune";
    }
  }

  /**
   * Get the Commune display page.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param code Code INSEE de la commune affichée
   * @param dateParam Date de validité de la commune passée en tant que paramètre optionnel 
   * @param communeSearchModel search details for the model.
   * @return the corresponding view.
   */
  @GetMapping("/{code}")
  public String getDisplayPage(final Locale locale,
                               final Model model,
                               @PathVariable("code") final String code,
                               @RequestParam(value = "date", required = false) final String dateParam,
                               @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    Date dateValidite = DateConversionUtils.urlStringToDate(dateParam, new Date());
    log.debug("Display commune: {}", code);
    if (code != null) {
      CommunePlusWithGenealogie commune = communePlusService.getCommuneValidOnDateWithGenealogie(code, dateValidite);
      if(commune != null) {
        Departement departement = departementService.getDepartementByCode(commune.getCommunePlus().getDepartement(), dateValidite);
        Region region = regionService.getRegionByCode(departement.getRegion(), dateValidite);
        return viewCommuneDisplay(locale, model, new CommuneDisplayModel(commune, departement, region));
      } else {
        model.addAttribute("errorRecherche", messageSource.getMessage("communesearch.error.noresult", null, locale));
        return viewCommuneSearch(locale, model, communeSearchModel);
      }
    } else {
      model.addAttribute("errorRecherche", messageSource.getMessage("communesearch.error.noresult", null, locale));
      return viewCommuneSearch(locale, model, communeSearchModel);
    }
  }

  /**
   * Setup model and return view for the search page.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return View for the search page
   */
  private String viewCommuneSearch(final Locale locale,
                                   final Model model,
                                   final CommuneSearchModel communeSearchModel) {
    model.addAttribute("titre", messageSource.getMessage("communesearch.title", null, locale));
    model.addAttribute("communeSearch", communeSearchModel);
    return "referentiel/communesearch";
  }

  /**
   * Setup model and return view for the result page.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeSearchModel search details for the model.
   * @return View for the result page
   */
  private String viewCommuneResults(final Locale locale,
                                    final Model model,
                                    final CommuneSearchModel communeSearchModel) {
    model.addAttribute("titre", messageSource.getMessage("communeresult.title", null, locale));
    model.addAttribute("communeSearch", communeSearchModel);
    return "referentiel/communeresults";
  }

  /**
   * Setup model and return view for the display page.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param communeDisplayModel entity details for the model.
   * @return View for the display page
   */
  private String viewCommuneDisplay(final Locale locale,
                                    final Model model,
                                    final CommuneDisplayModel communeDisplayModel) {
    model.addAttribute("titre", messageSource.getMessage("communedisplay.title", null, locale)
                                 + ": " + communeDisplayModel.getCodeInsee()
                                 + " " + communeDisplayModel.getNomEnrichi());
    model.addAttribute(COMMUNE_DISPLAY_MODEL, communeDisplayModel);
    return "referentiel/communedisplay";
  }

  /**
   * Export de la liste des communes au format Excel
   * @param response Servlet Response Object.
   * @param communeSearchModel Objet contenant la recherche
   */
  @PostMapping("/export")
  public void exportExcel(final HttpServletResponse response,
                          @ModelAttribute(COMMUNE_SEARCH_MODEL) final CommuneSearchModel communeSearchModel) {
    response.setContentType("application/vnd.ms-excel");
    response.setHeader("Content-Disposition",
                       "attachment; filename=\"" + DEFAULT_EXPORT_FILENAME + "\"");
    Export export = new ExportExcel();
    try (OutputStream out = response.getOutputStream()) {
      export.exportCommune(out, communeSearchModel.getCommunes());
      out.flush();
    } catch (IOException e) {
      log.info("Echec lors de l'export au format excel", e);
    }
  }
}
