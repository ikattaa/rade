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

import java.util.Locale;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import fr.aesn.rade.service.RegionService;
import fr.aesn.rade.webapp.mvc.referentiel.ReferentielController;
import fr.aesn.rade.webapp.mvc.referentiel.ReferentielSearchModel;
import javax.servlet.ServletContext;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

/**
 * Spring MVC Test for ReferentielController .
 *
 * @author Adrien GUILLARD (adrien.guillard@sully-group.fr)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class TestReferentielController
        extends AbstractTestController {

  /** Service for Regions. */
  @Autowired
  private RegionService regionService;
  /** Web application Context. */
  @Autowired
  private WebApplicationContext context;

  @InjectMocks
  private ReferentielController controller;
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
            .setScriptEncoding("UTF-8").setName("testdb")
            .addScript("db/sql/create-tables.sql")
            .addScript("db/sql/insert-TypeEntiteAdmin.sql")
            .addScript("db/sql/insert-TypeNomClair.sql")
            .addScript("db/sql/insert-Audit.sql")
            .addScript("db/sql/insert-Region.sql")
            .addScript("db/sql/insert-Departement.sql")
            .build();
  }

  /**
   * Set up the Test Environment.
   */
  @Before
  public void setUp() {
    controller = new ReferentielController();
    controller.setRegionService(regionService);
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setSingleView(mockView).build();
  }

  @Test
  public void testServletContext() {
    ServletContext servletContext = context.getServletContext();
    Assert.assertNotNull(servletContext);
    Assert.assertTrue(servletContext instanceof MockServletContext);
  }

  /**
   * Test getting the Entity Search page .
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testEntitySearchPage() throws Exception {
    this.mockMvc.perform(get("/referentiel/entiteSearch").locale(locale))
            .andExpect(status().isOk());
  }

  /**
   * Test getting Entity Search with an empty code insee as parameter.
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSearchEntityGetWithEmptyCode() throws Exception {
    this.mockMvc.perform(get("/referentiel/entiteSearch").locale(locale).param("codeInsee", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
  }

  /**
   * Test Entity Search with an empty code Insee and entity type is "commune".
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSearchEntityWithEmptyCode() throws Exception {
    ReferentielSearchModel referentielSearchModel = new ReferentielSearchModel();
    referentielSearchModel.setCode("");
    referentielSearchModel.setType("commune");
    this.mockMvc.perform(post("/referentiel/entiteSearch").locale(locale).param("valider", "").flashAttr("referentielSearchModel", referentielSearchModel))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
  }

  /**
   * Test Entity Search with an existing code Insee.
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSearchEntityWithCode() throws Exception {
    ReferentielSearchModel referentielSearchModel = new ReferentielSearchModel();
    referentielSearchModel.setCode("97402");
    referentielSearchModel.setType("commune");
    this.mockMvc.perform(post("/referentiel/entiteSearch").locale(locale).param("valider", "").flashAttr("referentielSearchModel", referentielSearchModel))
            .andExpect(status().isOk())
            .andExpect(view().name("redirect:/referentiel/commune/97402"));
  }

  
   /**
   * Test Entity Search with a null code Insee.
   * @throws Exception if there was an Exception processing request.
   */
  @Test
  public void testSearchEntityWithNullCode() throws Exception {
    ReferentielSearchModel referentielSearchModel = new ReferentielSearchModel();
    referentielSearchModel.setCode(null);
    referentielSearchModel.setType("commune");
    this.mockMvc.perform(post("/referentiel/entiteSearch").locale(locale).param("valider", "").flashAttr("referentielSearchModel", referentielSearchModel))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
  }
}
