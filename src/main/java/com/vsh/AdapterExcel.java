package com.vsh;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    static List<List<ObjectForValidation>> getDataFromExcel(String fullExcelFilePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(fullExcelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();
        List<ObjectForValidation> validationObjects = new ArrayList<>();
        List<List<ObjectForValidation>> allDataByString = new ArrayList<>();

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
            } else validationObjects.add(new ObjectForValidation(fieldName, 0, i));
        }

        //System.out.println(dataArray.toString());
        allDataByString.add(new ArrayList<>(validationObjects));
        validationObjects.clear();

        for (int j = 1; j < rowSize; j++) {
            row = sheet.getRow(j);
            for (int k = 0; k < columnCount; k++) {
                //TODO: сделать проверку что row не null (нет полностью пустой строки) иначе падает в ошибку NullPointerException
                validationObjects.add(new ObjectForValidation(formatter.formatCellValue(row.getCell(k)), j, k));
            }
            //  System.out.println("Строка " + j + " = " + dataArray.toString());
            allDataByString.add(new ArrayList<>(validationObjects));
            validationObjects.clear();
        }
        inputStream.close();
        return allDataByString;
    }

    static void writeDataToExcel(String fullExcelFilePath, ErrorList errorList) throws IOException {
        // Загружаем книгу из файла
        try (FileInputStream fis = new FileInputStream(fullExcelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Нулевой лист не найден");
            }

            List<ErrorObject> errorObjectList = errorList.getAllErrorObject();
            for (ErrorObject error : errorObjectList){
                cellSet(workbook, sheet, error.getRowExcel(), error.getColumnExcel(), error.getErrorMessage());
            }

            // Сохраняем книгу обратно в тот же файл
            try (FileOutputStream fos = new FileOutputStream(fullExcelFilePath)) {
                workbook.write(fos);
            }
        }
    }


    private static void cellSet(Workbook workbook, Sheet sheet, int rowIndex, int colIndex, String commentText) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        Cell cell = row.getCell(colIndex);
        if (cell == null) cell = row.createCell(colIndex);

        // Создаем рисунок (для комментариев)
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();

        // Задаем позицию и размер комментария рядом с ячейкой
        anchor.setCol1(colIndex);
        anchor.setCol2(colIndex + 14);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 18);

        Comment comment = cell.getCellComment();
        String newtext;
        if (comment == null) {
            // Создаем новый комментарий, если отсутствует
            comment = drawing.createCellComment(anchor);
            newtext = commentText;
        } else {
            // Если комментарий есть, просто обновим якорь
            comment.setAddress(1, 1);
            newtext = comment.getString() +"\n"+commentText;
        }

        // Устанавливаем текст и автора
        RichTextString richText = helper.createRichTextString(newtext);
        comment.setString(richText);
        comment.setAuthor("Alcyone");

        // Присваиваем комментарий ячейке
        cell.setCellComment(comment);
    }
}
