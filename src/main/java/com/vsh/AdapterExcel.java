package com.vsh;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
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

    private final IndexedColors color;
    private final FillPatternType type;
    private File file;


    public AdapterExcel(IndexedColors color, FillPatternType type) {
        this.color = color;
        this.type = type;
    }

    /**
     * Инициализация файла с проверкой на его существование
     *
     * @param file - файл с конфигурациями
     * @return true-если файл существует, false - если нет
     */
    public boolean setFile(File file) {
        if (!file.exists()) {
            System.out.println("Файл " + file.getAbsolutePath() + " не существует");
            return false;
        } else {
            this.file = file;
            return true;
        }
    }

    /**
     * Метод получения данных из Excel и раскладывания их в набор столбцов с данными - List<List<ObjectForValidation>>
     * для чтения используется file - Excel файл
     *
     * @return List<List < ObjectForValidation>> - список столбцов из Excel-я разложенный в объекты ObjectForValidation
     */
    public List<List<ObjectForValidation>> readFromExcel() {
        if (file == null) {
            System.out.println("Файл не проинициализирован - выполните setFile");
            return null;
        }
        Sheet sheet;
        try (Workbook workbook = new XSSFWorkbook(file)) {
            sheet = workbook.getSheetAt(0);
        } catch (NotOfficeXmlFileException | IOException | InvalidFormatException e) {
            System.out.println("Структура файла " + file.getAbsolutePath() + " не является Excel или файл заблокирован");
            return null;
            //throw new NotOfficeXmlFileException("Структура файла " + file.getAbsolutePath() + " не является Excel или файл заблокирован");
        }

        DataFormatter formatter = new DataFormatter();
        List<ObjectForValidation> validationObjects = new ArrayList<>();
        List<List<ObjectForValidation>> allDataByString = new ArrayList<>();
        System.out.println("Читаю данные из файла " + file.getAbsolutePath());
        int rowSize = sheet.getPhysicalNumberOfRows();
        //System.out.println("Общее количество непустых строк = " + rowSize);
        int columnCount;
        //Row row;
        Row row = sheet.getRow(0);
        if (row == null) {
            System.out.println("Ошибка в строке заголовков - пустая строка");
            return null;
        }
        columnCount = row.getLastCellNum(); // Вернёт количество столбцов в первой строке
        //System.out.println("Число столбцов в первой строке = " + columnCount);
        for (int i = 0; i < columnCount; i++) {
            String fieldName = formatter.formatCellValue(row.getCell(i));
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
            if (row == null) {
                System.out.println("Пустая строка номер " + (j + 1) + " в данных");
                return null;
                //continue; - такое решение позволит пропускать пустые строки
            }
            for (int k = 0; k < columnCount; k++) {
                validationObjects.add(new ObjectForValidation(formatter.formatCellValue(row.getCell(k)), j, k));
            }
            //  System.out.println("Строка " + j + " = " + dataArray.toString());
            allDataByString.add(new ArrayList<>(validationObjects));
            validationObjects.clear();
        }
        //inputStream.close();
        return allDataByString;
    }

    /**
     * Метод записи Примечаний в Excel-файл из списка ошибок (из объектов ObjectForValidation)
     * для записи используется file - Excel файл
     *
     * @param errorList - список ошибок из которого формируются Примечания
     */
    public void writeDataToExcel(ErrorList errorList) throws IOException {
        if (file == null) {
            System.out.println("Файл не проинициализирован - выполните setFile");
            return;
        }
        // Загружаем книгу из файла
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Нулевой лист не найден");
            }

            List<ErrorObject> errorObjectList = errorList.getAllErrorObject();
            for (ErrorObject error : errorObjectList) {
                cellSet(workbook, sheet, error.getRowExcel(), error.getColumnExcel(), error.getErrorMessage());
            }

            // Сохраняем книгу обратно в тот же файл
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Наполнение Примечания конкретной ячейки
     * если Примечание в ячейке не пустое - данные добавляются в то же Примечание на следующую строку
     *
     * @param workbook    - книга
     * @param sheet       - страница
     * @param rowIndex    - строка
     * @param colIndex    - столбец
     * @param commentText - текст, которым нужно заполнить примечание
     */
    private void cellSet(Workbook workbook, Sheet sheet, int rowIndex, int colIndex, String commentText) {
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

        CellStyle fillStyle = workbook.createCellStyle();

        //закрашиваю ячейку с ошибкой в нужный цвет
        fillStyle.setFillForegroundColor(color.getIndex());
        fillStyle.setFillPattern(type);
        cell.setCellStyle(fillStyle);

        //создаю примечание
        Comment comment = cell.getCellComment();
        String newText;
        if (comment == null) {
            comment = drawing.createCellComment(anchor);
            newText = commentText;
        } else {
            // Если комментарий есть, просто обновим якорь
            comment.setAddress(1, 1);
            newText = comment.getString() + "\n" + commentText;
        }

        // Устанавливаем текст и автора
        RichTextString richText = helper.createRichTextString(newText);
        comment.setString(richText);
        comment.setAuthor("Alcyone");

        // Присваиваем комментарий ячейке
        cell.setCellComment(comment);
    }
}
