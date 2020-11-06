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
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.validation.BindException;

import fr.aesn.rade.persist.model.CirconscriptionBassin;
import fr.aesn.rade.persist.model.CommuneSandre;
import fr.aesn.rade.service.BassinService;

import org.springframework.batch.item.file.transform.FieldSet;
import org.junit.Ignore;
/**
 * JUnit Test for CommuneSandreMapper.
 *
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Ignore
public class TestCommuneSandreMapper {
  /** Test line from the Sandre Commune file to import. */
  public static final String TEST_LINE_LONG =
    "\"14621\";\"SAINT-MARTIN-DE-BIENFAITE-LA-CRESSONNIÈRE\";\"Validé\";\"2002-01-01\";\"2016-01-01T00:00:00\";\"FR000003\";\"03\";\"EU31\";\"H\"";
  /** Test line from the Sandre Commune file to import. */
  public static final String TEST_LINE_NO_DATE =
    "\"31300\";\"LIEOUX\";\"Validé\";\"\";\"2016-01-01T00:00:00\";\"FR000005\";\"05\";\"FRF\";\"F\"";

  /** Metadata Service. */
  private static BassinService bassinService = mock(BassinService.class);

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
    Map<String, CirconscriptionBassin> map = MapUtils.putAll(new HashMap<String, CirconscriptionBassin>(),
                                                             new Object[][] {
        {"01", CirconscriptionBassin.of("01", "AP", "ARTOIS-PICARDIE", null)},
        {"02", CirconscriptionBassin.of("02", "RM", "RHIN-MEUSE", null)},
        {"03", CirconscriptionBassin.of("03", "SN", "SEINE-NORMANDIE", null)},
        {"04", CirconscriptionBassin.of("04", "LB", "LOIRE-BRETAGNE", null)},
        {"05", CirconscriptionBassin.of("05", "AG", "ADOUR-GARONNE", null)},
        {"06", CirconscriptionBassin.of("06", "RMED", "RHONE-MEDITERRANEE", null)},
        {"07", CirconscriptionBassin.of("07", "GUA", "GUADELOUPE", null)},
        {"08", CirconscriptionBassin.of("08", "MAR", "MARTINIQUE", null)},
        {"09", CirconscriptionBassin.of("09", "GUY", "GUYANE", null)},
        {"10", CirconscriptionBassin.of("10", "RE", "REUNION", null)},
        {"11", CirconscriptionBassin.of("11", "MAY", "MAYOTTE", null)},
        {"12", CirconscriptionBassin.of("12", "CO", "Corse", null)}
    });
    when(bassinService.getBassinMap()).thenReturn(map);
  }

  /**
   * Test mapping one line (long) from the Sandre Commune file to import.
   */
  @Test
  public void testMappingLong() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(";");
    FieldSet fieldSet = tokenizer.tokenize(TEST_LINE_LONG);
    CommuneSandreMapper mapper = new CommuneSandreMapper();
    mapper.setBassinService(bassinService);
    try {
      CommuneSandre commune = mapper.mapFieldSet(fieldSet);
      assertEquals("Entity doesn't match expected value",
                   "14621", commune.getCodeCommune());
      assertEquals("Entity doesn't match expected value",
                   "SAINT-MARTIN-DE-BIENFAITE-LA-CRESSONNIÈRE", commune.getLibelleCommune());
      assertEquals("Entity doesn't match expected value",
                   "Validé", commune.getStatutCommune());
      assertEquals("Entity doesn't match expected value",
                   "2002-01-01", sdf.format(commune.getDateCreationCommune()));
      assertEquals("Entity doesn't match expected value",
                   "2016-01-01", sdf.format(commune.getDateMajCommune()));
      assertEquals("Entity doesn't match expected value",
                   "FR000003", commune.getCodeComiteBassin());
      assertEquals("Entity doesn't match expected value",
                   "03", commune.getCirconscriptionBassin().getCode());
      assertEquals("Entity doesn't match expected value",
                   "EU31", commune.getCodeEuDistrict());
      assertEquals("Entity doesn't match expected value",
                   "H", commune.getCodeBassinDce());
      assertNull("Entity doesn't match expected null value",
                 commune.getAudit());
    } catch (BindException e) {
      fail("Mapper failed to parse test String with BindException: "
           + e.getMessage());
    }
  }

  /**
   * Test mapping one line (with no date) from the Sandre Commune file to import.
   */
  @Test
  public void testMappingNoDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(";");
    FieldSet fieldSet = tokenizer.tokenize(TEST_LINE_NO_DATE);
    CommuneSandreMapper mapper = new CommuneSandreMapper();
    mapper.setBassinService(bassinService);
    try {
      CommuneSandre commune = mapper.mapFieldSet(fieldSet);
      assertEquals("Entity doesn't match expected value",
                   "31300", commune.getCodeCommune());
      assertEquals("Entity doesn't match expected value",
                   "LIEOUX", commune.getLibelleCommune());
      assertEquals("Entity doesn't match expected value",
                   "Validé", commune.getStatutCommune());
      assertNull("Entity doesn't match expected null value",
                 commune.getDateCreationCommune());
      assertEquals("Entity doesn't match expected value",
                   "2016-01-01", sdf.format(commune.getDateMajCommune()));
      assertEquals("Entity doesn't match expected value",
                   "FR000005", commune.getCodeComiteBassin());
      assertEquals("Entity doesn't match expected value",
                   "05", commune.getCirconscriptionBassin().getCode());
      assertEquals("Entity doesn't match expected value",
                   "FRF", commune.getCodeEuDistrict());
      assertEquals("Entity doesn't match expected value",
                   "F", commune.getCodeBassinDce());
      assertNull("Entity doesn't match expected null value",
                 commune.getAudit());
    } catch (BindException e) {
      fail("Mapper failed to parse test String with BindException: "
           + e.getMessage());
    }
  }
}
