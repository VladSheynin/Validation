package com.vsh;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер получения данных из Excel
 *
 * @author Владислав Шейнин (начало разработки 18.07.2025)
 * @version 0.1
 */
public class AdapterExcel {

    static List<List<String>> getDataFromExcel(String fullExcelFilePath) throws IOException {

        FileInputStream inputStream = new FileInputStream(fullExcelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();
        List<String> dataArray = new ArrayList<>();
        List<List<String>> allDataByString = new ArrayList<>();

        int rowSize = sheet.getPhysicalNumberOfRows();
        //System.out.println("Общее количество непустых строк = " + rowSize);
        int columnCount;
        Row row;
        Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            System.out.println("Ошибка в строке заголовков - пустая строка");
            return null;
        }
        columnCount = firstRow.getLastCellNum(); // Вернёт количество столбцов в первой строке
        //System.out.println("Число столбцов в первой строке = " + columnCount);
        for (int i = 0; i < columnCount; i++) {
            String fieldName = formatter.formatCellValue(firstRow.getCell(i));
            if (fieldName.isEmpty()) {
                System.out.println("Ошибка в строке заголовков - есть пустые ячейки");
                return null;
            } else dataArray.add(fieldName);
        }

        //System.out.println(dataArray.toString());
        allDataByString.add(new ArrayList<>(dataArray));
        dataArray.clear();

        for (int j = 1; j < rowSize; j++) {
            row = sheet.getRow(j);
            for (int k = 0; k < columnCount; k++) {
                dataArray.add(formatter.formatCellValue(row.getCell(k)));
            }
            //  System.out.println("Строка " + j + " = " + dataArray.toString());
            allDataByString.add(new ArrayList<>(dataArray));
            dataArray.clear();
        }
        return allDataByString;
    }
}
