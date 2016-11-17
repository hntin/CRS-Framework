/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacoredemo;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

public class ExcelTool {

    public static void main(String[] args) throws Exception {
        /* Create Workbook and Worksheet */
        SXSSFWorkbook wb = new SXSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Chia Target");
        Sheet sheet2 = wb.createSheet();
        
        int rowIdx = 0;
        int colIdx = 0;
        sheet1.setColumnWidth(2, 10000);

        // Row 1
        Row row = sheet1.createRow(rowIdx);
        Cell cell = sheet1.getRow(rowIdx).createCell(0);
        XSSFCellStyle style = formatCell(wb, false, false, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), 
                HorizontalAlignment.CENTER, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 24, HSSFColor.RED.index);
        cell.setCellStyle(style);
        cell.setCellValue("Chia Target");
        sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Row 2
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, false, false, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.CENTER, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        for (colIdx = 0; colIdx <= 8; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
        }

        cell = sheet1.getRow(rowIdx).createCell(9);
        style = formatCell(wb, false, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.RED.getIndex(),
                HorizontalAlignment.CENTER, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        cell.setCellStyle(style);

        colIdx = 10;
        cell = sheet1.getRow(rowIdx).createCell(colIdx);
        style = formatCell(wb, false, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        cell.setCellStyle(style);
        cell.setCellValue("Vùng nhập liệu");

        //<editor-fold defaultstate="collapsed" desc="Table 1">
        // Row 3
        row = sheet1.createRow(++rowIdx);
        cell = sheet1.getRow(rowIdx).createCell(0);
        style = formatCell(wb, false, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.GENERAL, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        cell.setCellStyle(style);
        cell.setCellValue("ADMIN GIAO");

        // Row 4
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE_GREY.getIndex(),
                HorizontalAlignment.CENTER, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.WHITE.index);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
        }

        // Duyệt qua danh sách động các trials liên quan đến một campaign để xây dựng các columns động
        HashMap<String, Integer> trialList = new HashMap<>();
        trialList.put("CORE", 1);
        trialList.put("OPTION", 2);
        trialList.put("COMBO", 3);
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(s);
            colIdx++;
        }

        // Row 5 
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.BLACK.index);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            if (colIdx == 2) {
                cell.setCellValue("ADMIN giao");
            }
        }

        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.RIGHT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.BLACK.index);
        colIdx = 3;
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trialList.get(s));
            colIdx++;
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Table 2">
        // Row 6, 7
        row = sheet1.createRow(++rowIdx);
        row = sheet1.createRow(++rowIdx);
        cell = sheet1.getRow(rowIdx).createCell(0);
        style = formatCell(wb, false, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        cell.setCellStyle(style);
        cell.setCellValue("CHIA TARGET");

        // Row 8
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE_GREY.getIndex(), HorizontalAlignment.CENTER,
                "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.WHITE.index);

        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            if (colIdx == 0) {
                cell.setCellValue("STT");
            }
            if (colIdx == 1) {
                cell.setCellValue("CODE");
            }
            if (colIdx == 2) {
                cell.setCellValue("NAME");
            }
        }
        // Duyệt qua số tuần của Target, hiển thị số trials tương ứng được chia cho mỗi tuần.
        colIdx = 3;
        List<String> weekList = new ArrayList<String>();
        weekList.add("WEEK 39");
        weekList.add("WEEK 40");
        for (String s : weekList) {
            // Merge cell theo kích thước của trialList
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(s);
            for (int i = 0; i < trialList.size() - 1; i++) {
                cell = sheet1.getRow(rowIdx).createCell(++colIdx);
                cell.setCellStyle(style);
            }
            colIdx = colIdx - trialList.size() + 1;
            sheet1.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, colIdx, colIdx + trialList.size() - 1));
            colIdx += trialList.size();
        }

        cell = sheet1.getRow(rowIdx).createCell(colIdx);
        cell.setCellStyle(style);
        cell.setCellValue("TOTAL");
        for (int i = 0; i < trialList.size() - 1; i++) {
            cell = sheet1.getRow(rowIdx).createCell(++colIdx);
            cell.setCellStyle(style);
        }
        colIdx = colIdx - trialList.size() + 1;
        sheet1.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, colIdx, colIdx + trialList.size() - 1));

        // Row 9
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE_GREY.getIndex(), HorizontalAlignment.CENTER,
                "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.WHITE.index);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            sheet1.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx, colIdx, colIdx));
        }

        // Dữ liệu động - Begin
        for (String week : weekList) {
            for (String trial : trialList.keySet()) {
                cell = sheet1.getRow(rowIdx).createCell(colIdx);
                cell.setCellStyle(style);
                cell.setCellValue(trial);
                colIdx++;
            }
        }
        for (String trial : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trial);
            colIdx++;
        }
        // Dữ liệu động - End

        // Danh sách các SUB và Target_Detail tương ứng.
        List<TargetVersionDetailVO> listTargetVersionDetailVO = new ArrayList<TargetVersionDetailVO>();
        TargetVersionDetailVO targetVersion = new TargetVersionDetailVO();
        targetVersion.setClCode("HCM-CL");
        targetVersion.setClName("Nguyen Van A");
        listTargetVersionDetailVO.add(targetVersion);
        targetVersion = new TargetVersionDetailVO();
        targetVersion.setClCode("DN-CL");
        targetVersion.setClName("Nguyen Van B");
        listTargetVersionDetailVO.add(targetVersion);

        int orderNum = 1;
        for (TargetVersionDetailVO detail : listTargetVersionDetailVO) {
            row = sheet1.createRow(++rowIdx);
            for (colIdx = 0; colIdx <= 2; colIdx++) {
                cell = sheet1.getRow(rowIdx).createCell(colIdx);
                if (colIdx == 0) {
                    style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.CENTER, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.BLACK.index);
                    cell.setCellStyle(style);
                    cell.setCellValue(orderNum);
                }
                if (colIdx == 1) {
                    style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.BLACK.index);
                    cell.setCellStyle(style);
                    cell.setCellValue(detail.getClCode());
                }
                if (colIdx == 2) {
                    style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.BLACK.index);
                    cell.setCellStyle(style);
                    cell.setCellValue(detail.getClName());
                }

            }

            // Chèn dữ liệu target theo tuần.
            // Dữ liệu động - Begin
            for (int i=0; i< weekList.size(); i++) {
                for (String trial : trialList.keySet()) {
                    cell = sheet1.getRow(rowIdx).createCell(colIdx);
                    style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.RED.getIndex(),
                            HorizontalAlignment.RIGHT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.WHITE.index);
                    cell.setCellStyle(style);
                    cell.setCellValue(trial);
                    colIdx++;
                }
            }
            for (String trial : trialList.keySet()) {
                cell = sheet1.getRow(rowIdx).createCell(colIdx);
                style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.ORANGE.getIndex(),
                        HorizontalAlignment.RIGHT, "Arial", HSSFFont.COLOR_NORMAL, (short) 10, HSSFColor.WHITE.index);
                cell.setCellStyle(style);
                cell.setCellValue(trial);
                colIdx++;
            }
            // Dữ liệu động - End

            orderNum++;
        }

        //<editor-fold defaultstate="collapsed" desc="Yellow row">
        row = sheet1.createRow(++rowIdx);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.YELLOW.getIndex(),
                    HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
            if (colIdx == 0) {
                cell.setCellStyle(style);
            }
            if (colIdx == 1) {
                cell.setCellStyle(style);
                cell.setCellValue("ABC");
            }
            if (colIdx == 2) {
                cell.setCellStyle(style);
                cell.setCellValue("XYZ");
            }
        }

        // Chèn dữ liệu target theo tuần.
        // Dữ liệu động - Begin
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.YELLOW.getIndex(),
                    HorizontalAlignment.RIGHT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        for (String week : weekList) {
            for (String trial : trialList.keySet()) {
                cell = sheet1.getRow(rowIdx).createCell(colIdx);
                cell.setCellStyle(style);
                cell.setCellValue(trial);
                colIdx++;
            }
        }
        for (String trial : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trial);
            colIdx++;
        }
        // Dữ liệu động - End
        //</editor-fold>
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Table 3">
        row = sheet1.createRow(++rowIdx);
        row = sheet1.createRow(++rowIdx);
        cell = sheet1.getRow(rowIdx).createCell(0);
        style = formatCell(wb, false, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.BLACK.index);
        cell.setCellStyle(style);
        cell.setCellValue("CHÊNH LỆCH GIỮA TARGET ÁP VÀ TARGET NHẬN");

        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE_GREY.getIndex(), HorizontalAlignment.CENTER,
                "Arial", HSSFFont.BOLDWEIGHT_BOLD, (short) 10, HSSFColor.WHITE.index);

        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
        }
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(s);
            colIdx++;
        }

        // Dong "CL Chia"
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            if (colIdx == 2) {
                cell.setCellValue("CL Chia");
            }
        }
        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.RIGHT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        colIdx = 3;
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trialList.get(s));
            colIdx++;
        }

        // Dong "Admin giao"
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            if (colIdx == 2) {
                cell.setCellValue("Admin giao");
            }
        }
        style = formatCell(wb, true, true, FillPatternType.NO_FILL, IndexedColors.WHITE.getIndex(), HorizontalAlignment.RIGHT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        colIdx = 3;
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trialList.get(s));
            colIdx++;
        }

        // Dong "Chenh lech"
        row = sheet1.createRow(++rowIdx);
        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.YELLOW.getIndex(),
                HorizontalAlignment.LEFT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);

        for (colIdx = 0; colIdx <= 2; colIdx++) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            if (colIdx == 2) {
                cell.setCellValue("Chenh lech");
            }
        }

        style = formatCell(wb, true, false, FillPatternType.SOLID_FOREGROUND, IndexedColors.YELLOW.getIndex(),
                HorizontalAlignment.RIGHT, "Arial", HSSFFont.BOLDWEIGHT_NORMAL, (short) 10, HSSFColor.BLACK.index);
        colIdx = 3;
        for (String s : trialList.keySet()) {
            cell = sheet1.getRow(rowIdx).createCell(colIdx);
            cell.setCellStyle(style);
            cell.setCellValue(trialList.get(s));
            colIdx++;
        }
        //</editor-fold>

        /* Write changes to the workbook */
        FileOutputStream out = new FileOutputStream("/temp/JTI_Bieu_mau_ChiaTarget_.xlsx");
        wb.write(out);
        out.close();
        wb.dispose(); // dispose of temporary files backing this workbook on disk
    }

    private static XSSFCellStyle formatCell(SXSSFWorkbook wb, boolean isBolder, boolean isLock, 
            FillPatternType fillPattern, short fillColor, HorizontalAlignment alignment, 
            String fontName, short fontBold, short fontSize, short fontColor) {
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
        style.setLocked(isLock);
        style.setFillForegroundColor(fillColor);
        style.setFillPattern(fillPattern);
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (isBolder == true) {
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }

        Font font = wb.createFont();
        font.setFontName(fontName);
        font.setBoldweight(fontBold);
        font.setFontHeightInPoints((short) fontSize);
        font.setColor(fontColor);
        style.setFont(font);
        return style;
    }
}

class TargetVersionDetailVO {
    String clCode;
    String clName;

    public String getClCode() {
        return clCode;
    }

    public void setClCode(String clCode) {
        this.clCode = clCode;
    }

    public String getClName() {
        return clName;
    }

    public void setClName(String clName) {
        this.clName = clName;
    }

}
