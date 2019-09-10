/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Sophie Belin
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
package fr.aesn.rade.webapp.export;

import fr.aesn.rade.common.modelplus.CommunePlusWithGenealogie;
import fr.aesn.rade.common.modelplus.GenealogieSimple;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Export du résultat de la recherche de communes au format Excel.
 * @author sophie.belin
 */
@Slf4j
public class ExportExcel
  implements Export {
  /**
   * Export the given CommuneList to the given OutputStream.
   * @param os the OutputStream to which the export is written.
   * @param list the list of Communes to export.
   */
  @Override
  public void exportCommune(final OutputStream os,
                            final List<CommunePlusWithGenealogie> list) {
      if(list != null && list.size() > 0){
        Workbook wb = buildCommuneWorkbook(list);
        try {
          wb.write(os);
        } catch (IOException e) {
          log.info("Erreur lors de la génération de l'export excel", e);
        }
      }else{
          log.info("Erreur lors de la génération de l'export excel : la liste de communes est vide");
      }
  }

  /**
   * Build the Excel Workbook containing the given Communes list.
   * @param list the list of Communes to export.
   * @return the Excel Workbook containing the given Communes list.
   */
  private static Workbook buildCommuneWorkbook(final List<CommunePlusWithGenealogie> list) {
    Workbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet("Liste des communes");
    CreationHelper creationHelper = wb.getCreationHelper();
    CellStyle cs = wb.createCellStyle();
    cs.setDataFormat(creationHelper.createDataFormat().getFormat("DD/mm/yyyy"));
    CellStyle headercs = getHeaderCellStyle(wb);

    Row header1 = sheet.createRow(0);
    header1.createCell(0).setCellValue("Commune");
    header1.getCell(0).setCellStyle(headercs);
    header1.createCell(4).setCellValue("Entité mère");
    header1.getCell(4).setCellStyle(headercs);
    sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));
    sheet.addMergedRegion(new CellRangeAddress(0,0,4,5));
    Row header2 = sheet.createRow(1);
    header2.createCell(0).setCellValue("Code");
    header2.getCell(0).setCellStyle(headercs);
    header2.createCell(1).setCellValue("Nom");
    header2.getCell(1).setCellStyle(headercs);
    header2.createCell(2).setCellValue("Début validité");
    header2.getCell(2).setCellStyle(headercs);
    header2.createCell(3).setCellValue("Fin validité");
    header2.getCell(3).setCellStyle(headercs);
    header2.createCell(4).setCellValue("Motif de modification");
    header2.getCell(4).setCellStyle(headercs);
    header2.createCell(5).setCellValue("Code Insee");
    header2.getCell(5).setCellStyle(headercs);

    for(int i = 0; i < list.size(); i++) {
      Row row = sheet.createRow(i+2);
      CommunePlusWithGenealogie commune = list.get(i);
      row.createCell(0).setCellValue(commune.getCommunePlus().getCodeInsee());
      row.createCell(1).setCellValue(commune.getCommunePlus().getNomEnrichi());
      if(commune.getCommunePlus().getDebutValiditeCommuneInsee()!= null){
        row.createCell(2).setCellValue(commune.getCommunePlus().getDebutValiditeCommuneInsee());
        row.getCell(2).setCellStyle(cs);
      }
      if(commune.getCommunePlus().getFinValiditeCommuneInsee()!= null){
        row.createCell(3).setCellValue(commune.getCommunePlus().getFinValiditeCommuneInsee());
        row.getCell(3).setCellStyle(cs);
      }
      
      StringBuilder sb = new StringBuilder();
      String motif = null;
      for (Map.Entry<String, GenealogieSimple> entry : commune.getParents().entrySet()) {
        sb.append(entry.getKey());
        sb.append(" ");
        motif = entry.getValue().getTypeModification().getLibelleLong();
      }
      if(motif != null){
        row.createCell(4).setCellValue(motif);
        row.getCell(4).setCellStyle(cs);
      }
      row.createCell(5).setCellValue(sb.toString().trim());
    }
    sheet.autoSizeColumn(0);
    sheet.autoSizeColumn(1);
    sheet.autoSizeColumn(2);
    sheet.autoSizeColumn(3);
    sheet.autoSizeColumn(4);
    sheet.autoSizeColumn(5);
    return wb;
  }

  /**
   * Cell Style for header.
   * @param wb
   * @return
   */
  private static CellStyle getHeaderCellStyle(final Workbook wb) {
    CellStyle styleHeader = wb.createCellStyle();
    Font fontHeader = wb.createFont();
    fontHeader.setBold(true);
    styleHeader.setFont(fontHeader);
    styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styleHeader.setWrapText(true);
    styleHeader.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
    styleHeader.setAlignment(HorizontalAlignment.CENTER);
    styleHeader.setBorderBottom(BorderStyle.THIN);
    styleHeader.setBorderLeft(BorderStyle.THIN);
    styleHeader.setBorderRight(BorderStyle.THIN);
    styleHeader.setBorderTop(BorderStyle.THIN);
    return styleHeader;
  }
}
