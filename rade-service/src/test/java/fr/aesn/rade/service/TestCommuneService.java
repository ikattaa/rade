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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import fr.aesn.rade.common.InvalidArgumentException;
import fr.aesn.rade.common.util.StringConversionUtils;
import fr.aesn.rade.persist.dao.CommuneJpaDao;
import fr.aesn.rade.persist.dao.GenealogieEntiteAdminJpaDao;
import fr.aesn.rade.persist.dao.StatutModificationJpaDao;
import fr.aesn.rade.persist.dao.TypeEntiteAdminJpaDao;
import fr.aesn.rade.persist.dao.TypeGenealogieEntiteAdminJpaDao;
import fr.aesn.rade.persist.dao.TypeNomClairJpaDao;
import fr.aesn.rade.persist.model.Audit;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.GenealogieEntiteAdmin;
import fr.aesn.rade.persist.model.TypeEntiteAdmin;
import fr.aesn.rade.persist.model.TypeNomClair;
import fr.aesn.rade.service.impl.CommuneServiceImpl;
import fr.aesn.rade.service.impl.MetadataServiceImpl;

/**
 * JUnit Test for DelegationService.
 * 
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCommuneService
  extends AbstractTestService {
  /** DAO for the Service to be tested. */
  @Autowired
  private CommuneJpaDao communeJpaDao;
  /** Data Access Object for TypeEntiteAdmin. */
  @Autowired
  private GenealogieEntiteAdminJpaDao genealogieEntiteAdminJpaDao;
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
  /** Service  to be tested. */
  private CommuneService service;

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
        .addScript("db/sql/insert-Commune-Test.sql")
        .addScript("db/sql/insert-CommuneGenealogie-Test.sql")
        .build();
  }

  /**
   * Set up the Test Environment.
   */
  @Before
  public void setUp() {
    MetadataService metadataService = new MetadataServiceImpl();
    ((MetadataServiceImpl)metadataService).setTypeEntiteAdminJpaDao(typeEntiteAdminJpaDao);
    ((MetadataServiceImpl)metadataService).setTypeGenealogieEntiteAdminJpaDao(typeGenealogieEntiteAdminJpaDao);
    ((MetadataServiceImpl)metadataService).setTypeNomClairJpaDao(typeNomClairJpaDao);
    ((MetadataServiceImpl)metadataService).setStatutModificationJpaDao(statutModificationJpaDao);
    service = new CommuneServiceImpl();
    ((CommuneServiceImpl)service).setCommuneJpaDao(communeJpaDao);
    ((CommuneServiceImpl)service).setGenealogieEntiteAdminJpaDao(genealogieEntiteAdminJpaDao);
    ((CommuneServiceImpl)service).setMetadataService(metadataService);
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList() {
    List<Commune> list = service.getAllCommune();
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(639, list.size());
    for (Commune commune : list) {
      assertNotNull("Hibernate returned a List but an Entity is null",
                    commune);
    }
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList2018() {
    Date year2018 = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune(year2018);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(632, list.size());
    for (Commune commune : list) {
      assertNotNull("Hibernate returned a List but an Entity is null",
                    commune);
    }
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList1998() {
    Date year1998 = Date.from(ZonedDateTime.of(1998, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune(year1998);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(0, list.size());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList2018dept91() {
    Date year2018 = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune("91", "", year2018);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(196, list.size());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList2018dept99() {
    Date year2018 = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune("99", "", year2018);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(0, list.size());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList1998dept91() {
    Date year1998 = Date.from(ZonedDateTime.of(1998, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune("91", "", year1998);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(0, list.size());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList2018nomSaint() {
    Date year2018 = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune("", "Saint", year2018);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(71, list.size());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneList2018Dept92nomANTO() {
    Date year2018 = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    List<Commune> list = service.getAllCommune("92", "ANTO", year2018);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(1, list.size());
  }

  /**
   * Test getting a Communes.
   * @throws ParseException failed to parse date.
   */
  @Test
  public void testGettingCommuneById() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Commune com = service.getCommuneById(37570);
    assertNotNull("Hibernate didn't return a Commune", com);
    assertEquals("Hibernate returned a Commune, but the Id doesn't match",
                 37570, com.getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 sdf.parse("1999-01-01"), com.getDebutValidite());
    assertNull("Hibernate returned a Commune, but a field doesn't match",
               com.getFinValidite());
    assertNull("Hibernate returned a Commune, but a field doesn't match",
               com.getArticleEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "BASSE-TERRE", com.getNomMajuscule());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "Basse-Terre", com.getNomEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "", com.getCommentaire());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "0", com.getTypeNomClair().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "COM", com.getTypeEntiteAdmin().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 1, com.getAudit().getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "97105", com.getCodeInsee());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "971", com.getDepartement());
    assertNull(service.getCommuneById(0));
    assertNull(service.getCommuneById(-1));
    assertNull(service.getCommuneById(2000000));
  }

  /**
   * Test getting a the list of all Communes.
   * @throws ParseException failed to parse date.
   */
  @Test
  public void testGettingCommuneByCode() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    List<Commune> list = service.getCommuneByCode("97105");
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(1, list.size());
    Commune com = list.get(0);
    assertNotNull("Hibernate didn't return a Commune", com);
    assertEquals("Hibernate returned a Commune, but the Id doesn't match",
                 37570, com.getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 sdf.parse("1999-01-01"), com.getDebutValidite());
    assertNull("Hibernate returned a Commune, but a field doesn't match",
               com.getFinValidite());
    assertNull("Hibernate returned a Commune, but a field doesn't match",
               com.getArticleEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "BASSE-TERRE", com.getNomMajuscule());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "Basse-Terre", com.getNomEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "", com.getCommentaire());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "0", com.getTypeNomClair().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "COM", com.getTypeEntiteAdmin().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 1, com.getAudit().getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "97105", com.getCodeInsee());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "971", com.getDepartement());
  }

  /**
   * Test getting a the list of all Communes.
   */
  @Test
  public void testGettingCommuneForDate() {
    List<Commune> list;
    Date date = Date.from(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    list = service.getAllCommune(date);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(632, list.size());
    for (Commune commune : list) {
      assertNotNull("Hibernate returned a List but an Entity is null",
                    commune);
    }
    date = Date.from(ZonedDateTime.of(1998, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
    list = service.getAllCommune(date);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(0, list.size());
    date = null; // i.e. current date
    list = service.getAllCommune(date);
    assertNotNull("CommuneService returned a null list", list);
    assertEquals(632, list.size());
  }

  /**
   * Tests MOD=100 : Changement de Nom.
   * @throws ParseException failed to parse date.
   * @throws InvalidArgumentException passed wrong arguments during
   * CommuneService request.
   */
  @Test
  public void testMod100()
    throws ParseException, InvalidArgumentException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Set<GenealogieEntiteAdmin> genealogie;
    // Check the commune has no children
    Commune commune = service.getCommuneByCode("97105", "2018-03-01");
    genealogie = commune.getEnfants();
    assertEquals(0, genealogie.size());
    // Modify the Commune and test it's new values
    Commune newCommune = service.mod10ChangementdeNom(sdf.parse("2018-06-01"), commune.getAudit(), "97105", "0", "Basse-Terre 2","BASSE TERRE 2", null);
    assertNotNull("Hibernate didn't return a Commune", newCommune);
    assertNotEquals("Hibernate returned a Commune, but the Id doesn't match",
                    37570, newCommune.getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 sdf.parse("2018-06-01"), newCommune.getDebutValidite());
    assertNull("Hibernate returned a Commune, but a field doesn't match",
               newCommune.getFinValidite());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "", newCommune.getArticleEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "BASSE-TERRE 2", newCommune.getNomMajuscule());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "Basse-Terre 2", newCommune.getNomEnrichi());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "", newCommune.getCommentaire());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "0", newCommune.getTypeNomClair().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "COM", newCommune.getTypeEntiteAdmin().getCode());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 1, newCommune.getAudit().getId().intValue());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "97105", newCommune.getCodeInsee());
    assertEquals("Hibernate returned a Commune, but a field doesn't match",
                 "971", newCommune.getDepartement());
    // Check the new genealogie
    genealogie = newCommune.getEnfants();
    assertEquals(0, genealogie.size());
    genealogie = newCommune.getParents();
    assertEquals(1, genealogie.size());
    assertEquals(37570, genealogie.iterator().next().getParentEnfant().getParent().getId().intValue());
    Commune parent = service.getCommuneById(37570);
    // Check the original commune now has a child
    genealogie = parent.getEnfants();
    assertEquals(1, genealogie.size());
    assertEquals(newCommune.getId(), genealogie.iterator().next().getParentEnfant().getEnfant().getId());
  }

  /**
   * Tests MOD=200 : Creation.
   * @throws ParseException failed to parse date.
   * @throws InvalidArgumentException passed wrong arguments during
   * CommuneService request.
   */
  @Test
  public void testMod20()
    throws ParseException, InvalidArgumentException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Audit audit = service.getCommuneById(37570).getAudit(); // re-use existing Audit
    Commune newCommune = service.mod20Creation(sdf.parse("2019-01-01"), audit, "01999", "01", "0", "Nouvelle Commune","NOUVELLE COMMUNE", null);
    Commune commune;
    commune = service.getCommuneByCode("01999", "2018-01-01");
    assertNull(commune);
    commune = service.getCommuneByCode("01999", "2019-01-02");
    assertNotNull(commune);
    assertEquals(newCommune, commune);
  }

  /**
   * Tests MOD=210 : Retablissement, MOD=230 : Commune se separant.
   * @throws ParseException failed to parse date.
   * @throws InvalidArgumentException passed wrong arguments during
   * CommuneService request.
   */
  @Test
  public void testMod21()
    throws ParseException, InvalidArgumentException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Audit audit = service.getCommuneById(37570).getAudit(); // re-use existing Audit
    Commune communeSource = service.getCommuneById(37344); // Chevilly-Larue
    Commune com210retabli = buildCommune("94100",
                                         communeSource.getDepartement(),
                                         sdf.parse("2019-01-01"),
                                         communeSource.getTypeNomClair(),
                                         "Larue", null, null);
    Commune com230source = buildCommune(communeSource.getCodeInsee(),
                                        communeSource.getDepartement(),
                                        sdf.parse("2019-01-01"),
                                        communeSource.getTypeNomClair(),
                                        "Chevilly", null, null);
    service.mod21Retablissement(sdf.parse("2019-01-01"), audit, com210retabli, com230source, null);
  }

  private Commune buildCommune(final String codeInsee,
                               final String departement,
                               final Date debutValidite,
                               final TypeNomClair tncc,
                               final String nomEnrichi,
                               final String nomMajuscule,
                               final String commentaire)
    throws InvalidArgumentException {
    Commune commune = new Commune();
    commune.setTypeEntiteAdmin(TypeEntiteAdmin.of("COM", "Commune"));
    commune.setCodeInsee(codeInsee);
    commune.setDepartement(departement);
    commune.setDebutValidite(debutValidite);
    commune.setTypeNomClair(tncc);
    commune.setArticleEnrichi(tncc.getArticle());
    commune.setNomEnrichi(nomEnrichi);
    commune.setNomMajuscule(nomMajuscule == null ? StringConversionUtils.toUpperAscii(nomEnrichi)
                                : nomMajuscule);
    commune.setCommentaire(commentaire == null ? ""
                              : commentaire);
    return commune;
}
}
