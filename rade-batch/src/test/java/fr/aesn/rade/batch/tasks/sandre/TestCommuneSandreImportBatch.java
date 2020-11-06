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
package fr.aesn.rade.batch.tasks.sandre;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

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
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.aesn.rade.batch.tasks.SpringBatchTestConfiguration;
import fr.aesn.rade.persist.dao.CommuneSandreJpaDao;
import fr.aesn.rade.persist.model.CommuneSandre;
import org.junit.Ignore;
/**
 * Test the Commune Sandre Batch Import Job.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCommuneSandreImportBatch {
  /** Static Spring Configuration. */
  @Configuration
  @ImportResource(locations = "classpath*:batch-job-sandre.xml")
  protected static class Config extends SpringBatchTestConfiguration {
  }
  /** In Memory Derby Database Instance. */
  protected static EmbeddedDatabase db;
  /** Spring Context. */
  @Autowired
  protected ApplicationContext context;
  /** JPA DAO used to check data imported by Batch. */
  @Autowired
  protected CommuneSandreJpaDao communeSandreJpaDao;
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
        .addScript("db/sql/insert-Audit.sql")
        .addScript("db/sql/insert-CirconscriptionBassin.sql")
        .build();
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
    jobLauncherTestUtils.setJob(context.getBean("importCommuneSandreJob", Job.class));
  }

  /**
   * Close the Test Environment cleanly.
   */
  @After
  public void tearDown() {
  }

  @Test
  public void testImportCommuneSandreJob() throws Exception {
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", "classpath:batchfiles/sandre/COM_20181016_SANDRE.csv");
    jobBuilder.addString("auditAuteur", "Batch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import Test");
    JobParameters jobParameters = jobBuilder.toJobParameters();
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    List<CommuneSandre> list = communeSandreJpaDao.findAll();
    assertEquals("The batch imported the wrong number of lines",
                 35885, list.size());
  }
}
