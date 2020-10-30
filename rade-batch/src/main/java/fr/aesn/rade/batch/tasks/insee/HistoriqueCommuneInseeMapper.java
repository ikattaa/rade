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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import lombok.extern.slf4j.Slf4j;

/**
 * FieldSetMapper that Maps INSEE Commune History file lines to
 * HistoriqueCommuneInseeModel.
 *
 * Parsed using the following Spring Batch Configuration:
 * <code>
 *   <property name="lineMapper">
 *     <bean class="org.springframework.batch.item.file.mapping.DefaultLineMapper">
 *       <property name="lineTokenizer">
 *         <bean class="org.springframework.batch.item.file.transform.DelimitedLineTokenizer"
 *               p:delimiter="&#9;"/> <!-- &#9; for TAB (ASCII code 09) -->
 *       </property>
 *       <property name="fieldSetMapper">
 *         <bean class="fr.aesn.rade.batch.tasks.insee.HistoriqueCommuneInseeMapper"/>
 *       </property>
 *     </bean>
 *   </property>
 * </code>
 * Example file:
 * <code>
 * DEP AR CT COM LEG         JO         EFF        DTR        MOD C_LOFF C_LANC NBCOM RANGCOM COMECH POPECH SUECH DEPANC ARRANC CTANC TNCCOFF NCCOFF            TNCCANC NCCANC
 * 01        003 A16-07-1973 19-08-1973 01-01-1974 01-01-1974 330                             01165                                   1       Amareins
 * 01        003 A07-12-1982            01-01-1983 01-01-1983 350                             01165                                   1       Amareins
 * 01  1  01 004 D25-03-1955 30-03-1955 31-03-1955 31-03-1955 100                                                                     1       Ambérieu-en-Bugey 1       Ambérieu
 * 01  4  15 014 D17-12-1996 24-12-1996 01-01-1997 01-01-1997 610                             01283  0      0                         1       Arbent
 * 01  4  15 014 D17-12-1996 24-12-1996 01-01-1997 01-01-1997 630                             01283  0      0                         1       Arbent
 * 01     04 015 A29-09-2015 24-12-2015 01-01-2016 01-01-2016 331                             01015                                   1       Arbignieu
 * 01  1  04 015 A29-09-2015 24-12-2015 01-01-2016 01-01-2016 341               2     2       01340                                   1       Arboys en Bugey
 * 01  1  04 015 A29-09-2015 24-12-2015 01-01-2016 01-01-2016 341               2     1       01015                                   1       Arboys en Bugey
 * 01        018 A09-12-1970 29-12-1970 01-01-1971 01-01-1971 310                             01033                                   1       Arlod
 * </code>
 * For more details, see:
 * https://www.insee.fr/fr/information/3363419
 *
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
public class HistoriqueCommuneInseeMapper
  implements FieldSetMapper<HistoriqueCommuneInseeModel> {
  /**
   * Maps INSEE Commune History file lines to HistoriqueCommuneInseeModel.
   * @param fieldSet parsed line from INSEE Commune History file.
   * @return the HistoriqueCommuneInseeModel.
   * @throws BindException if the FieldSet could not be mapped.
   */
  @Override
  public HistoriqueCommuneInseeModel mapFieldSet(final FieldSet fieldSet)
    throws BindException {
    log.trace("Importing line: {}", fieldSet.toString());
    HistoriqueCommuneInseeModel historique = new HistoriqueCommuneInseeModel();
    historique.setTypeEvenCommune(fieldSet.readString(0));
    historique.setDateEffet(fieldSet.readDate(1, "dd/MM/yy"));
    historique.setTypeCommuneAvantEven(fieldSet.readString(2));
    historique.setCodeCommuneAvantEven(fieldSet.readString(3));
    historique.setTypeNomClairAv(fieldSet.readString(4));
    historique.setNomClairMajAv(fieldSet.readString(5));
    historique.setNomClairTypographieRicheAv(fieldSet.readString(6));
    historique.setNomClairTypographieRicheAvecArticleAv(fieldSet.readString(7));
    historique.setTypeCommuneAprEven(fieldSet.readString(8));
    historique.setCodeCommuneaprEven(fieldSet.readString(9));
    historique.setTypeNomClairAp(fieldSet.readString(10));
    historique.setNomClairMajAp(fieldSet.readString(11));
    historique.setNomClairTypographieRicheAp(fieldSet.readString(12));
    historique.setNomClairTypographieRicheAvecArticleAp(fieldSet.readString(13));
    return historique;
  }

  /**
   * The Date JO field is a list of between 0 and 4 dates in the format
   * "dd-MM-yyyy", with no space between them.
   * @param dateJO the field to parse.
   * @return a list of Dates parsed from the given String.
   * @throws IllegalArgumentException if the dates could not be parsed
   * (just like fieldSet.readDate).
   */
  private List<Date> buildDateJoList(String dateJO) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    assert dateJO.length() % 10 == 0;
    int size = dateJO.length() / 10;
    ArrayList<Date> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      try {
        list.add(sdf.parse(dateJO.substring(i*10, (i+1)*10)));
      } catch (ParseException e) {
        log.info("Error parsing JO date.", e);
        throw new IllegalArgumentException(e);
      }
    }
    return list;
  }
}
