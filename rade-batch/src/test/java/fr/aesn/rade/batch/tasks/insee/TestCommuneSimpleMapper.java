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
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.collections4.MapUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.validation.BindException;

import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.TypeEntiteAdmin;
import fr.aesn.rade.persist.model.TypeNomClair;
import fr.aesn.rade.service.MetadataService;

import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.junit.Ignore;
/**
 * JUnit Test for CommuneMapper.
 *
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Ignore
public class TestCommuneSimpleMapper {
  /** Test line from the INSEE Departement file to import. */
  public static final String TEST_LINE =
    "0\t4\t01\t971\t05\t1\t06\t0\t\tBASSE-TERRE\t\tBasse-Terre";

  /** Metadata Service. */
  private static MetadataService metadataService = mock(MetadataService.class);

  /**
   * Set up the Test Environment.
   */
  @BeforeClass
  public static void setUpClass() {
    createServiceMocks();
  }

  /**
   * Build Service Mocks for use in the tests.
   */
  private static void createServiceMocks() {
      Map<String, TypeEntiteAdmin> tea = MapUtils.putAll(new HashMap<String, TypeEntiteAdmin>(),
                                                         new Object[][] {
        {"COM", TypeEntiteAdmin.of("COM", "Commune")},
        {"ARR", TypeEntiteAdmin.of("ARR", "Arrondissement")},
        {"CAN", TypeEntiteAdmin.of("CAN", "Canton")},
        {"DEP", TypeEntiteAdmin.of("DEP", "Département")},
        {"REG", TypeEntiteAdmin.of("REG", "Région")}
    });
    when(metadataService.getTypeEntiteAdminMap()).thenReturn(tea);
    Map<String, TypeNomClair> tncc = MapUtils.putAll(new HashMap<String, TypeNomClair>(),
                                                     new Object[][] {
        {"0", TypeNomClair.of("0", "", "DE", "")},
        {"1", TypeNomClair.of("1", "", "D'", "")},
        {"2", TypeNomClair.of("2", "Le", "DU", "LE")},
        {"3", TypeNomClair.of("3", "La", "DE LA", "LA")},
        {"4", TypeNomClair.of("4", "Les", "DES", "LES")},
        {"5", TypeNomClair.of("5", "L'", "DE L'", "L'")},
        {"6", TypeNomClair.of("6", "Aux", "DES", "AUX")},
        {"7", TypeNomClair.of("7", "Las", "DE LAS", "LAS")},
        {"8", TypeNomClair.of("8", "Los", "DE LOS", "LOS")}
    });
    when(metadataService.getTypeNomClairMap()).thenReturn(tncc);
  }

  /**
   * Test mapping one line from the Commune file to import.
   * @throws BindException Mapper failed to parse test String.
   */
  @Test
  public void testMapping() throws BindException {
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter("\t");
    FieldSet fieldSet = tokenizer.tokenize(TEST_LINE);
    CommuneSimpleMapper mapper = new CommuneSimpleMapper();
    mapper.setMetadataService(metadataService);
    Commune commune = mapper.mapFieldSet(fieldSet);
    assertEquals("Entity doesn't match expected value",
                 "COM", commune.getTypeEntiteAdmin().getCode());
    assertEquals("Entity doesn't match expected value",
                 "971", commune.getDepartement());
    assertEquals("Entity doesn't match expected value",
                 "97105", commune.getCodeInsee());
    assertEquals("Entity doesn't match expected value",
                 "0", commune.getTypeNomClair().getCode());
    assertEquals("Entity doesn't match expected value",
                 "BASSE-TERRE", commune.getNomMajuscule());
    assertEquals("Entity doesn't match expected value",
                 "", commune.getArticleEnrichi());
    assertEquals("Entity doesn't match expected value",
                 "Basse-Terre", commune.getNomEnrichi());
    assertNull("Entity doesn't match expected null value",
               commune.getCommentaire());
  }

  /**
   * Test mapping the whole Commune file to import.
   * @throws Exception problem reading/mapping input file.
   */
  @Test
  public void testMappingFile() throws Exception {
    // Configure and open ItemReader (reading test input file)
    FlatFileItemReader<Commune> reader = new FlatFileItemReader<>();
    reader.setResource(new ClassPathResource("batchfiles/insee/comsimp2018.txt"));
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
    // Configure Validator and validate (@Size, @Min, ...) each line
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Commune record;
    Set<ConstraintViolation<Commune>> violations;
    int i = 0;
    while((record = reader.read()) != null) {
      violations = validator.validate(record);
      assertEquals("Record violates constraints", 0, violations.size());
      i++;
    }
    // Check all records from input file have been read
    assertNull(record);
    assertEquals(35357, i);
  }
}
