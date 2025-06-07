package org.example.bot.accountBot.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ExcelExportUtil {
    public static void exportToExcel(List<Map<String, String>> keys, HttpServletResponse response) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TRON_KEYS");

        // Header
        Row header = sheet.createRow(0);
        String[] titles = {"地址", "公钥", "私钥"};
        for (int i = 0; i < titles.length; i++) {
            header.createCell(i).setCellValue(titles[i]);
        }

        // Data
        for (int i = 0; i < keys.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, String> key = keys.get(i);
            row.createCell(0).setCellValue(key.get("address"));
            row.createCell(1).setCellValue(key.get("publicKey"));
            row.createCell(2).setCellValue(key.get("privateKey"));
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tron_keys.xlsx");
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        out.close();
        workbook.close();
    }
}

