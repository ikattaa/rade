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
package fr.aesn.rade.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsMapContaining;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.service.CommunePlusService;
import fr.aesn.rade.service.DepartementService;
import fr.aesn.rade.service.RegionService;
import fr.aesn.rade.webapp.mvc.referentiel.CommuneController;
import fr.aesn.rade.webapp.mvc.referentiel.CommuneSearchModel;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Spring MVC Test for CommuneController .
 *
 * @author Adrien GUILLARD (adrien.guillard@sully-group.fr)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class TestCommuneController
  extends AbstractTestController {
  /** Service for Communes. */
  @Autowired
  private CommunePlusService communeService;
  /** Service for Departements. */
  @Autowired
  private DepartementService deptService;
  /** Service for Regions. */
  @Autowired
  private RegionService regionService;
  /** I18n message source */
  @Autowired
  private MessageSource messageSource;
  /** Web application Context. */
  @Autowired
  private WebApplicationContext context;

  @InjectMocks
  private CommuneController controller;
  @Mock
  private View mockView;
  private MockMvc mockMvc;
  Locale locale = Locale.ENGLISH;

  /**
   * Set up the Test Environment.
   */
  @BeforeClass
  public static void setUpClass() {
    // create temporary database for Hibernate
    db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY)
            .setScriptEncoding("UTF-8")
            .setName("testdb")
            .addScript("db/sql/create-tables.sql")
            .addScript("db/sql/insert-StatutModification.sql")
            .addScript("db/sql/insert-TypeEntiteAdmin.sql")
            .addScript("db/sql/insert-TypeGenealogieEntiteAdmin.sql")
            .addScript("db/sql/insert-TypeNomClair.sql")
            .addScript("db/sql/insert-Audit.sql")
            .addScript("db/sql/insert-CirconscriptionBassin.sql")
            .addScript("db/sql/insert-Region.sql")
            .addScript("db/sql/insert-Departement.sql")
            .addScript("db/sql/insert-CommuneSandre-Test.sql")
            .addScript("db/sql/insert-Commune-Test.sql")
            .addScript("db/sql/insert-CommuneGenealogie-Test.sql")
            .build();
  }

  protected MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames("i18n/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(true);
    messageSource.setCacheSeconds(-1);
    messageSource.setAlwaysUseMessageFormat(false);
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  /**
   * Set up the Test Environment.
   */
  @Before
  public void setUp() {
    controller = new CommuneController();
    messageSource = messageSource();
    controller.setCommunePlusService(communeService);
    controller.setDepartementService(deptService);
    controller.setRegionService(regionService);
    controller.setMessageSource(messageSource);
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
                             .setSingleView(mockView)
                             .build();
  }

  @Test
  public void testServletContext() {
    ServletContext servletContext = context.getServletContext();
    Assert.assertNotNull(servletContext);
    Assert.assertTrue(servletContext instanceof MockServletContext);
  }

  /**
   * Test getting the Commune Search page (in French).
   */
  @Test
  public void testGetCommuneSearchPageFrench() throws Exception {
    mockMvc.perform(get("/referentiel/commune").locale(Locale.FRENCH))
           .andExpect(status().isOk())
           .andExpect(model().attribute("titre", "Rechercher une Commune"))
           .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test getting the Commune Search page (in English).
   */
  @Test
  public void testGetCommuneSearchPageEnglish() throws Exception {
    mockMvc.perform(get("/referentiel/commune").locale(Locale.ENGLISH))
           .andExpect(status().isOk())
           .andExpect(model().attribute("titre", "Commune Search"))
           .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test getting the Commune display page.
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetCommuneDisplayPage() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    mockMvc.perform(get("/referentiel/commune/97402?date=2018-01-21"))
           .andExpect(status().isOk())
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeInsee", is("97402"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("motifModification", is("Fusion-association : commune absorbante"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomEnrichi", is("Bras-Panon"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("commentaireModification", nullValue())))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("article", is(""))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("articleEnrichi", is("Lzs"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeBassin", is("03"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomBassin", is("SEINE-NORMANDIE"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeDepartement", is("974"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomDepartement", is("La Réunion"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomMajuscule", is("BRAS-PANON"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomRegion", is("La Réunion"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("finValidite", nullValue())))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasKey("95019"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("code", is("95019"))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("nom", is("Arnouville-lès-Gonesse"))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2010-07-11")))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasKey("95259"))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("code", is("95259"))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("nom", is("Gadancourt"))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2018-01-01")))))))
           .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))
           .andExpect(view().name("referentiel/communedisplay"));
  }

  /**
   * Test getting the Commune Display page without date.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetDisplayPageWithoutDate() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    mockMvc.perform(get("/referentiel/commune/97402"))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeInsee", is("97402"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("motifModification", is("Fusion-association : commune absorbante"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomEnrichi", is("Bras-Panon"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("commentaireModification", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("article", is(""))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("articleEnrichi", is("Lzs"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeBassin", is("03"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomBassin", is("SEINE-NORMANDIE"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeDepartement", is("974"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomDepartement", is("La Réunion"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomMajuscule", is("BRAS-PANON"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomRegion", is("La Réunion"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("finValidite", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasKey("95019"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("code", is("95019"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("nom", is("Arnouville-lès-Gonesse"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2010-07-11")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasKey("95259"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("code", is("95259"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("nom", is("Gadancourt"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2018-01-01")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))
            .andExpect(view().name("referentiel/communedisplay"));
  }

  /**
   * Test getting the Commune display page without Parent entities and daughter
   * entities.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetDisplayPageWithoutEntities() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    mockMvc.perform(get("/referentiel/commune/97415?date=2019-03-28"))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeInsee", is("97415"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("motifModification", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomEnrichi", is("Saint-Paul"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("commentaireModification", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("article", is(""))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("articleEnrichi", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeBassin", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomBassin", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("codeDepartement", is("974"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomDepartement", is("La Réunion"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomMajuscule", is("SAINT-PAUL"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("nomRegion", is("La Réunion"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("finValidite", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("parents", Matchers.aMapWithSize(0))))
            .andExpect(model().attribute(CommuneController.COMMUNE_DISPLAY_MODEL, hasProperty("enfants", Matchers.aMapWithSize(0))))
            .andExpect(view().name("referentiel/communedisplay"));
  }

  /**
   * Test getting Commune display page for a non-existant commune.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetCommuneDisplayPageError() throws Exception {
    mockMvc.perform(get("/referentiel/commune/1000?date=2000-01-01").locale(locale))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorRecherche", is(messageSource.getMessage("communesearch.error.noresult", null, locale))))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test getting the Search Form page.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetSearchForm() throws Exception {

    mockMvc.perform(get("/referentiel/commune/").locale(locale))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeInsee", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeRegion", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeDepartement", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeCirconscription", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("nomEnrichi", nullValue())))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communesearch.title", null, locale))))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test getting the search result list .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetResultList() throws Exception {
    CommuneSearchModel searchCommune = new CommuneSearchModel();
    searchCommune.setCommunes(new ArrayList<>());
    searchCommune.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97402", new Date()));
    searchCommune.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97401", new Date()));
    mockMvc.perform(get("/referentiel/commune/resultats").locale(locale).flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, searchCommune))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(2))))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communeresult.title", null, locale))))
            .andExpect(view().name("referentiel/communeresults"));
  }

  /**
   * Test getting the Result List with more then 1 Commune adding the page in URL .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testGetResultListWithMorethenCommunes() throws Exception {

    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCommunes(new ArrayList<>());
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97401", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97402", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97403", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97404", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97405", new Date()));

    mockMvc.perform(get("/referentiel/commune/resultats?page=1").locale(locale).flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(5))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("page", is(1))))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communeresult.title", null, locale))))
            .andExpect(view().name("referentiel/communeresults"));
  }

  /**
   * Test getting the search result list when the commune is null(redirect).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testResultListRedirect() throws Exception {
    mockMvc.perform(get("/referentiel/commune/resultats"))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeInsee", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeRegion", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeDepartement", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeCirconscription", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("nomEnrichi", nullValue())))
            .andExpect(view().name("redirect:/referentiel/commune"));
  }

  /**
   * Test postting Reset of the search form without parameters (annuler).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testResetSearchForm() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    mockMvc.perform(post("/referentiel/commune/resultats").locale(locale).param("annuler", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeInsee", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeRegion", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeDepartement", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeCirconscription", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("nomEnrichi", nullValue())))
            .andExpect(view().name("redirect:/referentiel/commune"));
  }

  /**
   * Test of search reset of the search form with parameters(annuler)
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testresetSearchForm() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("03");
    communeSearchModel.setCodeDepartement("974");
    communeSearchModel.setCodeRegion("10");
    communeSearchModel.setCodeInsee("69100");
    communeSearchModel.setCommunes(new ArrayList<>());
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97415", new Date()));
    communeSearchModel.setDateEffet(sdf.parse("2000-01-01"));
    communeSearchModel.setNomEnrichi("panon");
    mockMvc.perform(post("/referentiel/commune/resultats").param("annuler", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeInsee", nullValue())))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeRegion", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeDepartement", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("codeCirconscription", is("-1"))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("nomEnrichi", nullValue())))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune"));
  }

  /**
   * Test postting Submit (POST) of search form (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchForm_withoutParameters() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setCommunes(null);

    // null
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", nullValue())))
            .andExpect(view().name("referentiel/communesearch"));

    // empty
    communeSearchModel.setCodeInsee("");
    communeSearchModel.setNomEnrichi("");
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", nullValue())))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test postting Submit (POST) the search form (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultList() throws Exception {
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").param("nomEnrichi", "ville"))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(66))))
            .andExpect(view().name("redirect:/referentiel/commune/resultats?page=1"));
  }

  /**
   * Test postting Submit (POST) the search form without parametres.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultListEmpty() throws Exception {
    mockMvc.perform(post("/referentiel/commune/resultats").locale(locale).param("valider", ""))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", messageSource.getMessage("communesearch.error.empty", null, locale)))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", nullValue())))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test postting Submit (POST) the search form without parametres.
   *
   * @throws Exception if there was an Exception processing request.
   */

  @Test
  public void testPostResultListCommuneIsEmpty() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCommunes(new ArrayList<>());
    mockMvc.perform(post("/referentiel/commune/resultats").locale(locale).param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", messageSource.getMessage("communesearch.error.empty", null, locale)))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communesearch.title", null, locale))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(0))))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test postting Submit (POST) of the search form Commune is null .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultListCommuneIsNull() throws Exception {

    CommuneSearchModel searchCommune = new CommuneSearchModel();
    searchCommune.setCommunes(new ArrayList<>());
    searchCommune.setCodeInsee("10000");
    searchCommune.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97402", new Date()));
    mockMvc.perform(post("/referentiel/commune/resultats").locale(locale).param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, searchCommune))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", messageSource.getMessage("communesearch.error.noresult", null, locale)))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communesearch.title", null, locale))))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test postting Submit (POST) of the search form Commune is null .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultListSizeCommune() throws Exception {

    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCommunes(new ArrayList<>());
    communeSearchModel.setCodeInsee("97402");
    mockMvc.perform(post("/referentiel/commune/resultats?page=1").locale(locale).param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(1))))
            .andExpect(view().name("redirect:/referentiel/commune/97402?date=" + DateConversionUtils.toUrlString(new Date())));
  }

  /**
   * Test postting Submit (POST) the search form with less then 10 Communes(tray to go in the second page) .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultListSizeCommunePage1() throws Exception {

    CommuneSearchModel searchCommune = new CommuneSearchModel();
    searchCommune.setCommunes(new ArrayList<>());
    searchCommune.setCodeInsee("97402");
    mockMvc.perform(post("/referentiel/commune/resultats?page=2").locale(locale).param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, searchCommune))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", Matchers.hasSize(1))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("page", is(1))))
            .andExpect(view().name("redirect:/referentiel/commune/97402?date=" + DateConversionUtils.toUrlString(new Date())));
  }

  /**
   * Test postting Submit (POST) of the search form more then 10 Communes(tow Pages) .
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testPostResultListPage2() throws Exception {

    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCommunes(new ArrayList<>());
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97401", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97402", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97403", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97404", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97405", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97406", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97407", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97408", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97409", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97410", new Date()));
    communeSearchModel.getCommunes().add(communeService.getCommuneValidOnDateWithGenealogie("97411", new Date()));

    mockMvc.perform(post("/referentiel/commune/resultats?page=2").locale(locale).param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(status().isOk())
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(11))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("page", is(2))))
            .andExpect(model().attribute("titre", is(messageSource.getMessage("communesearch.title", null, locale))))
            .andExpect(view().name("referentiel/communesearch"));
  }

  /**
   * Test of search form with codeInsee validation.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithCodeInsee() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeInsee("97402");
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(1))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("codeInsee", is("97402")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("articleEnrichi", is("Lzs")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("circonscriptionBassin", hasProperty("code", is("03"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("debutValiditeCommuneInsee", is(sdf.parse("1999-01-01"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("departement", is("974")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("finValiditeCommuneInsee", nullValue()))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomEnrichi", is("Bras-Panon")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomMajuscule", is("BRAS-PANON")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasKey("95259"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("code", is("95259"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("nom", is("Gadancourt"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2018-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasKey("95019"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("code", is("95019"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("nom", is("Arnouville-lès-Gonesse"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2010-07-11")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/97402?date=" + DateConversionUtils.toUrlString(new Date())));
  }

  /**
   * Test of search form with nom validation.
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithNom() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi("panon");
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(1))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("codeInsee", is("97402")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("articleEnrichi", is("Lzs")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("circonscriptionBassin", hasProperty("code", is("03"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("debutValiditeCommuneInsee", is(sdf.parse("1999-01-01"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("departement", is("974")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("finValiditeCommuneInsee", nullValue()))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomEnrichi", is("Bras-Panon")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomMajuscule", is("BRAS-PANON")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasKey("95259"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("code", is("95259"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("nom", is("Gadancourt"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2018-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasKey("95019"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("code", is("95019"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("nom", is("Arnouville-lès-Gonesse"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2010-07-11")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/97402?date=" + DateConversionUtils.toUrlString(new Date())));
  }

  /**
   * Test of search form with departement (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithDepartement() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeDepartement("974");
    communeSearchModel.setCodeRegion("-1");
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(24))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/resultats?page=1"));
  }

  /**
   * Test of search form with region (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithCodeRegion() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeDepartement("-1");
    communeSearchModel.setCodeRegion("11");
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(503))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/resultats?page=1"));
  }

  /**
   * Test of search form with bassin (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithCodeBassin() throws Exception {
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("03");
    communeSearchModel.setCodeDepartement("-1");
    communeSearchModel.setCodeRegion("-1");
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(2))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/resultats?page=1"));
  }

  /**
   * Test of search form inactive commune (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  public void testSubmitSearchFormWithInactiveCommune() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeCirconscription("-1");
    communeSearchModel.setCodeDepartement("-1");
    communeSearchModel.setCodeRegion("-1");
    communeSearchModel.setCodeInsee("95259");
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(sdf.parse("2000-01-01"));
    communeSearchModel.setNomEnrichi(null);
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(1))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("codeInsee", is("95259")))))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/95259?date=2000-01-01"));
  }

  /**
   * Test of search form with all parameters (validation).
   *
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSubmitSearchFormWithDepartementAndRegionAndBassinAndName() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    CommuneSearchModel communeSearchModel = new CommuneSearchModel();
    communeSearchModel.setCodeDepartement("974");
    communeSearchModel.setCodeRegion("04");
    communeSearchModel.setCodeCirconscription("03");
    communeSearchModel.setCodeInsee(null);
    communeSearchModel.setCommunes(null);
    communeSearchModel.setDateEffet(new Date());
    communeSearchModel.setNomEnrichi("panon");
    mockMvc.perform(post("/referentiel/commune/resultats").param("valider", "").flashAttr(CommuneController.COMMUNE_SEARCH_MODEL, communeSearchModel))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", hasSize(1))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("codeInsee", is("97402")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("articleEnrichi", is("Lzs")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("circonscriptionBassin", hasProperty("code", is("03"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("debutValiditeCommuneInsee", is(sdf.parse("1999-01-01"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("departement", is("974")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("finValiditeCommuneInsee", nullValue()))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomEnrichi", is("Bras-Panon")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("communePlus", hasProperty("nomMajuscule", is("BRAS-PANON")))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasKey("95259"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("code", is("95259"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("nom", is("Gadancourt"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2018-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("enfants", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasKey("95019"))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("code", is("95019"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("nom", is("Arnouville-lès-Gonesse"))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("debutValidite", is(sdf.parse("1999-01-01")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("finValidite", is(sdf.parse("2010-07-11")))))))))
            .andExpect(model().attribute(CommuneController.COMMUNE_SEARCH_MODEL, hasProperty("communes", contains(hasProperty("parents", IsMapContaining.hasValue(hasProperty("commentaire", nullValue())))))))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/97402?date=" + DateConversionUtils.toUrlString(new Date())));
  }
}
