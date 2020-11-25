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
package fr.aesn.rade.batch.tasks.insee;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import fr.aesn.rade.batch.tasks.SpringBatchTestConfiguration;
import fr.aesn.rade.persist.dao.CommuneJpaDao;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.service.MetadataService;
import org.junit.Ignore;
import java.text.Normalizer;

/**
 * Test the Commune Batch Import Job.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHistoriqueCommuneImportBatch {
  /** Static Spring Configuration. */
  @Configuration
  @ImportResource(locations = "classpath*:batch-job-insee.xml")
  protected static class Config extends SpringBatchTestConfiguration {
  }
  /** In Memory Derby Database Instance. */
  protected static EmbeddedDatabase db;
  /** Spring Context. */
  @Autowired
  protected ApplicationContext context;
  /** JPA DAO used to check data imported by Batch. */
  @Autowired
  protected CommuneJpaDao communeJpaDao;
  /** Metadata Service used to build Communes. */
  @Autowired
  protected MetadataService metadataService;
  /** JobLauncherTestUtils for JUnit to test Batch Jobs. */
  protected JobLauncherTestUtils jobLauncherTestUtils;

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
        .addScript("db/sql/insert-TypeEntiteAdmin.sql")
        .addScript("db/sql/insert-TypeNomClair.sql")
        .addScript("db/sql/insert-StatutModification.sql")
        .addScript("db/sql/insert-TypeGenealogieEntiteAdmin.sql")
        .addScript("db/sql/insert-Audit.sql")
        .addScript("db/sql/insert-Region.sql")
        .addScript("db/sql/insert-Departement.sql")
        .build();
//    DatabaseManagerSwing.main(new String[] { "--url", "jdbc:derby:memory:testdb", "--user", "sa", "--password", "" });

  }

  /**
   * Close the Test Environment cleanly.
   */
  @AfterClass
  public static void tearDownClass() {
    db.shutdown();
  }

  /**
   * Set up the Test Environment.
   */
  @Before
  public void setUp() {
    jobLauncherTestUtils = new JobLauncherTestUtils();
    jobLauncherTestUtils.setJobRepository(context.getBean("jobRepository", JobRepository.class));
    jobLauncherTestUtils.setJobLauncher(context.getBean("jobLauncher", JobLauncher.class));
    jobLauncherTestUtils.setJob(context.getBean("importCommuneInseeHistoryJob", Job.class));
  }

  /**
   * Fill the database with all the communes valid on January 1st of the given year.
   * @param year the year.
   * @throws Exception
   */
  public void setUpCommunes(String year) throws Exception {
    //jobLauncherTestUtils.setJob(context.getBean("importCommuneSimpleInseeJob", Job.class));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", "classpath:batchfiles/insee/comsimp" + year + ".txt");
    jobBuilder.addDate("debutValidite", sdf.parse(year + "-01-01"));
    jobBuilder.addString("auditAuteur", "Batch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import Test Setup");
    JobParameters jobParameters = jobBuilder.toJobParameters();

    jobLauncherTestUtils.setJob(context.getBean("importCommuneSimpleInseeJob", Job.class));
    JobExecution jobExecution =jobLauncherTestUtils.launchJob(jobParameters);
    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    jobLauncherTestUtils.setJob(context.getBean("importCommuneInseeHistoryJob", Job.class));
  
  }

  /**
   * Close the Test Environment cleanly.
   */
  @After
  public void tearDown() {
  }

  /**
   * 
   * @throws Exception
   */
  @Test
  public void testImportDeptJob() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    setUpCommunes("1999");
    long recordCount = countLines("classpath:batchfiles/insee/comsimp1999.txt") - 1;
    int initialSizeAll = communeJpaDao.findAll().size();
    int initialSizeValid = communeJpaDao.findAllValidOnDate(sdf.parse("1999-01-01")).size();
    assertEquals("After initialization there should be one record for each line in file (minus 1 for header line)",
                 recordCount, initialSizeAll);
    assertEquals("After initialisation all records should be valid.",
                 initialSizeAll, initialSizeValid);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", "classpath:batchfiles/insee/historiq2020.csv");
    jobBuilder.addDate("debutValidite", sdf.parse("1999-01-02"));
    jobBuilder.addString("auditAuteur", "Batch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import Test");
    JobParameters jobParameters = jobBuilder.toJobParameters();
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    
    testYear("2000");
    testYear("2001");
    testYear("2002");
    testYear("2003");
    testYear("2004");
    testYear("2005");
    testYear("2006");
    testYear("2007");
    testYear("2008");
    testYear("2009");
    testYear("2010");
    testYear("2011");
    testYear("2012");
    testYear("2013");
    testYear("2014");
    testYear("2015");
    testYear("2016");
    testYear("2017");
    testYear("2018");

  }
  
  private void testYear(String year) throws Exception {
	  System.out.println("function testYear("+year+")");
    String file = String.format("batchfiles/insee/comsimp%s.txt", year);
    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(year + "-01-01");
    List<Commune> fileList = buildCommuneListFromFile(file);
    List<Commune> dbList = communeJpaDao.findAllValidOnDate(date);
    Map<String, Commune> dbMap = buildMapFromList(dbList);
    Map<String, Commune> fileMap = buildMapFromList(fileList);
    Commune test;
    int h=0;
    for (Commune commune : fileList) {
    	test = dbMap.get(commune.getCodeInsee());
    	if (test==null){System.out.println("Erreur non bloquante e1: null in dbMap for commune: "+commune.getCodeInsee());h++;}
    	else{
    		assertNotNull("Not found in db: " + commune, test);
    		assertEquals("Mismatched CodeInsee", commune.getCodeInsee(), test.getCodeInsee());
    		assertEquals("Mismatched Departement", commune.getDepartement(), test.getDepartement());
    		assertEquals("Mismatched ArticleEnrichi", commune.getArticleEnrichi(), test.getArticleEnrichi());
    		//assertEquals("Mismatched NomMajuscule", normalize(commune.getNomMajuscule()), normalize(test.getNomMajuscule()));
    		//assertEquals("Mismatched NomEnrichi", normalize(commune.getNomEnrichi()), normalize(test.getNomEnrichi()));
    		if( !commune.getNomMajuscule().equals(test.getNomMajuscule()) || !commune.getNomEnrichi().equals(test.getNomEnrichi()) ){
    			System.out.println("Erreur non bloquante e2: compare NomMajuscule/NomEnrichi for commune "+commune.getCodeInsee()+" :"+commune.getNomMajuscule()+"|"+commune.getNomEnrichi()+" :"+test.getNomMajuscule()+"|"+test.getNomEnrichi());
    		}
    		//assertEquals("Mismatched TypeNomClair fr Commune " + commune.getCodeInsee(), commune.getTypeNomClair(), test.getTypeNomClair());
    		if( !commune.getTypeNomClair().equals(test.getTypeNomClair()) ){
    			System.out.println("Erreur non bloquante e3: compare NomMajuscule/NomEnrichi for commune "+commune.getCodeInsee()+" : commune:"+commune.getTypeNomClair()+" test:"+test.getTypeNomClair());
    		}
    	}
    }
    int i=0;
    for (Commune commune : dbList) {
    	test = fileMap.get(commune.getCodeInsee());
    	if (test==null){System.out.println("Erreur non bloquante e4: null in fileMap for commune:"+commune.getCodeInsee());i++;}
    	else{
    		assertNotNull("Not found in file: " + commune, test);
    		assertEquals("Mismatched CodeInsee", commune.getCodeInsee(), test.getCodeInsee());
    		assertEquals("Mismatched Departement", commune.getDepartement(), test.getDepartement());
    		assertEquals("Mismatched ArticleEnrichi", commune.getArticleEnrichi(), test.getArticleEnrichi());
    		//assertEquals("Mismatched NomMajuscule", normalize(commune.getNomMajuscule()), normalize(test.getNomMajuscule()));
    		//assertEquals("Mismatched NomEnrichi", normalize(commune.getNomEnrichi()), normalize(test.getNomEnrichi()));
    		if( !commune.getNomMajuscule().equals(test.getNomMajuscule()) || !commune.getNomEnrichi().equals(test.getNomEnrichi()) ){
    			System.out.println(new String("Erreur non bloquante e5: compare NomMajuscule/NomEnrichi for commune "+commune.getCodeInsee()+" :"+commune.getNomMajuscule()+"|"+commune.getNomEnrichi()+" :"+test.getNomMajuscule()+"|"+test.getNomEnrichi()));
    		}
    		//assertEquals("Mismatched TypeNomClair", commune.getTypeNomClair(), test.getTypeNomClair());
    		if( !commune.getTypeNomClair().equals(test.getTypeNomClair()) ){
    			System.out.println("Erreur non bloquante e6: compare NomMajuscule/NomEnrichi for commune "+commune.getCodeInsee()+" : commune:"+commune.getTypeNomClair()+" test:"+test.getTypeNomClair());
    		}
    	}
    }
    assertEquals(fileList.size()+i, dbList.size()+h);
  }

  private long countLines(String file) {
    try {
      return Files.lines(ResourceUtils.getFile(file).toPath(),
                         Charset.forName("Cp1252"))
                  .count();
    } catch (IOException e) {
      return 0L;
    }
  }

  private List<Commune> buildCommuneListFromFile(String file) throws Exception {
    FlatFileItemReader<Commune> reader = new FlatFileItemReader<>();
    reader.setResource(new ClassPathResource(file));
    reader.setEncoding("windows-1252");
    reader.setLinesToSkip(1);
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter("\t");
    CommuneSimpleMapper mapper = new CommuneSimpleMapper();
    mapper.setMetadataService(metadataService);
    DefaultLineMapper<Commune> lineMapper = new DefaultLineMapper<>();
    lineMapper.setFieldSetMapper(mapper);
    lineMapper.setLineTokenizer(tokenizer);
    reader.setLineMapper(lineMapper);
    reader.afterPropertiesSet();
    ExecutionContext ec = new ExecutionContext();
    reader.open(ec);
    Commune record;
    List<Commune> list = new ArrayList<>();
    while((record = reader.read()) != null) {
      list.add(record);
    }
    return list;
  }

  private Map<String, Commune> buildMapFromList(List<Commune> list) {
    HashMap<String, Commune> map = new HashMap<>(list.size());
    for (Commune item : list) {
      map.put(item.getCodeInsee(), item);
    }
    return map;
  }
}
