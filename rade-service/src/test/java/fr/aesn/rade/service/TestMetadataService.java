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

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import fr.aesn.rade.persist.dao.StatutModificationJpaDao;
import fr.aesn.rade.persist.dao.TypeEntiteAdminJpaDao;
import fr.aesn.rade.persist.dao.TypeGenealogieEntiteAdminJpaDao;
import fr.aesn.rade.persist.dao.TypeNomClairJpaDao;
import fr.aesn.rade.persist.model.StatutModification;
import fr.aesn.rade.persist.model.TypeEntiteAdmin;
import fr.aesn.rade.persist.model.TypeGenealogieEntiteAdmin;
import fr.aesn.rade.persist.model.TypeNomClair;
import fr.aesn.rade.service.impl.MetadataServiceImpl;

/**
 * JUnit Test for DelegationService.
 * 
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
public class TestMetadataService
  extends AbstractTestService {
  /** Data Access Object for TypeEntiteAdmin. */
  @Autowired
  private TypeEntiteAdminJpaDao typeEntiteAdminJpaDao;
  /** Data Access Object for TypeGenealogieEntiteAdmin. */
  @Autowired
  private TypeGenealogieEntiteAdminJpaDao typeGenealogieEntiteAdminJpaDao;
  /** Data Access Object for TypeNomClair. */
  @Autowired
  private TypeNomClairJpaDao typeNomClairJpaDao;
  /** Data Access Object for StatutModification. */
  @Autowired
  private StatutModificationJpaDao statutModificationJpaDao;
  /** Service to be tested. */
  private MetadataService service;

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
        .build();
  }

  /**
   * Set up the Test Environment.
   */
  @Before
  public void setUp() {
    service = new MetadataServiceImpl();
    ((MetadataServiceImpl)service).setTypeEntiteAdminJpaDao(typeEntiteAdminJpaDao);
    ((MetadataServiceImpl)service).setTypeGenealogieEntiteAdminJpaDao(typeGenealogieEntiteAdminJpaDao);
    ((MetadataServiceImpl)service).setTypeNomClairJpaDao(typeNomClairJpaDao);
    ((MetadataServiceImpl)service).setStatutModificationJpaDao(statutModificationJpaDao);
  }

  /**
   * 
   */
  @Test
  public void testGetStatutModification() {
    StatutModification statutModification = service.getStatutModification("I");
    assertNotNull("Hibernate didn't return a StatutModification", statutModification);
    assertEquals("Hibernate returned a StatutModification, but the Code doesn't match",
                 "I", statutModification.getCode());
    assertEquals("Hibernate returned a StatutModification, but the Label doesn't match",
                 "Identifié", statutModification.getLibelleCourt());
    assertEquals("Hibernate returned a StatutModification, but the Label doesn't match",
                 "Identifié (à traiter)", statutModification.getLibelleLong());
  }

  /**
   * 
   */
  @Test
  public void testGetStatutModificationList() {
    List<StatutModification> objs = service.getStatutModificationList();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 3, objs.size());
    for (StatutModification obj: objs) {
      assertNotNull("Hibernate returned a List but an Entity is null", obj);
    }
  }

  /**
   * 
   */
  @Test
  public void testGetStatutModificationMap() {
    Map<String, StatutModification> objs = service.getStatutModificationMap();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 3, objs.size());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeEntiteAdmin() {
    TypeEntiteAdmin typeEntiteAdmin = service.getTypeEntiteAdmin("COM");
    assertNotNull("Hibernate didn't return a TypeEntiteAdmin", typeEntiteAdmin);
    assertEquals("Hibernate returned a TypeEntiteAdmin, but the Code doesn't match",
                 "COM", typeEntiteAdmin.getCode());
    assertEquals("Hibernate returned a TypeEntiteAdmin, but the Label doesn't match",
                 "Commune", typeEntiteAdmin.getLibelleCourt());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeEntiteAdminList() {
    List<TypeEntiteAdmin> objs = service.getTypeEntiteAdminList();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 5, objs.size());
    for (TypeEntiteAdmin obj: objs) {
      assertNotNull("Hibernate returned a List but an Entity is null", obj);
    }
  }

  /**
   * 
   */
  @Test
  public void testGetTypeEntiteAdminMap() {
    Map<String, TypeEntiteAdmin> objs = service.getTypeEntiteAdminMap();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 5, objs.size());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeGenealogieEntiteAdmin() {
    TypeGenealogieEntiteAdmin typeGenealogieEntiteAdmin = service.getTypeGenealogieEntiteAdmin("100");
    assertNotNull("Hibernate didn't return a TypeGenealogieEntiteAdmin", typeGenealogieEntiteAdmin);
    assertEquals("Hibernate returned a TypeGenealogieEntiteAdmin, but the Code doesn't match",
                 "100", typeGenealogieEntiteAdmin.getCode());
    assertEquals("Hibernate returned a TypeGenealogieEntiteAdmin, but the Label doesn't match",
                 "Changement de nom", typeGenealogieEntiteAdmin.getLibelleCourt());
    assertEquals("Hibernate returned a TypeGenealogieEntiteAdmin, but the Label doesn't match",
                 "Changement de nom", typeGenealogieEntiteAdmin.getLibelleLong());
    assertEquals("Hibernate returned a TypeGenealogieEntiteAdmin, but the default status doesn't match",
                 "I", typeGenealogieEntiteAdmin.getStatutParDefaut().getCode());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeGenealogieEntiteAdminList() {
    List<TypeGenealogieEntiteAdmin> objs = service.getTypeGenealogieEntiteAdminList();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 63, objs.size());
    for (TypeGenealogieEntiteAdmin obj: objs) {
      assertNotNull("Hibernate returned a List but an Entity is null", obj);
    }
  }

  /**
   * 
   */
  @Test
  public void testGetTypeGenealogieEntiteAdminMap() {
    Map<String, TypeGenealogieEntiteAdmin> objs = service.getTypeGenealogieEntiteAdminMap();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 63, objs.size());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeNomClair() {
    TypeNomClair typeNomClair = service.getTypeNomClair("8");
    assertNotNull("Hibernate didn't return a TypeNomClair", typeNomClair);
    assertEquals("Hibernate returned a TypeNomClair, but the Code doesn't match",
                 "8", typeNomClair.getCode());
    assertEquals("Hibernate returned a TypeNomClair, but the Article doesn't match",
                 "Los", typeNomClair.getArticle());
    assertEquals("Hibernate returned a TypeNomClair, but the Charniere doesn't match",
                 "DE LOS", typeNomClair.getCharniere());
    assertEquals("Hibernate returned a TypeNomClair, but the Article Majuscule doesn't match",
                 "LOS", typeNomClair.getArticleMaj());
  }

  /**
   * 
   */
  @Test
  public void testGetTypeNomClairList() {
    List<TypeNomClair> objs = service.getTypeNomClairList();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 9, objs.size());
    for (TypeNomClair obj: objs) {
      assertNotNull("Hibernate returned a List but an Entity is null", obj);
    }
  }

  /**
   * 
   */
  @Test
  public void testGetTypeNomClairMap() {
    Map<String, TypeNomClair> objs = service.getTypeNomClairMap();
    assertNotNull("Hibernate didn't return a List", objs);
    assertEquals("Hibernate returned a List, but the wrong size",
                 9, objs.size());
  }
}
