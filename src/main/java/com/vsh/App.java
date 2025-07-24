package com.vsh;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс
 */
public class App {

    private static final String excelFileFullPathName = "C:\\Projects\\Validation\\CheckTest.xlsx";
    private static final String configsFileFullPathName = "C:\\Projects\\Validation\\configOTP.json";
    private static final IndexedColors color = IndexedColors.PINK;
    private static final FillPatternType type = FillPatternType.DIAMONDS;

    public static void main(String[] args) {
        List<ErrorList> errorLists = new ArrayList<>();
        AdapterExcel adapterExcel;
        CheckValidationHelper helper;
        try {
            adapterExcel = new AdapterExcel(color, type, new File(excelFileFullPathName));
            helper = new CheckValidationHelper(new File(configsFileFullPathName));
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка в файлах");
            return;
        }

        List<List<ObjectForValidation>> allDataFromExcel;
        try {
            allDataFromExcel = adapterExcel.readFromExcel();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        if (allDataFromExcel == null) {
            System.out.println("Данные не разобраны или файл пустой");
            return;
        }

        List<ObjectForValidation> columnIterator = new ArrayList<>();
        ErrorList errorList;
        boolean hasErrorAllFile = false;
        for (int j = 0; j < allDataFromExcel.get(0).size(); j++) {
            columnIterator.clear();
            for (List<ObjectForValidation> object : allDataFromExcel) {
                columnIterator.add(object.get(j));
            }

            errorList = new ErrorList("Столбец " + columnIterator.get(0).getDataForCheck());
            errorLists.add(errorList);    //создаю отдельные очереди для каждого столбца,

            //TODO: распараллелить проверки по отдельным потокам

            // получаем объект конфигурации и стартуем с ним проверки
            ConfigObjectForValidation config = helper.getConfigurationObject(columnIterator.get(0).getDataForCheck());
            if (config == null) {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " не найден");
            } else {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " найден. Стартую проверки");
                if (!helper.validation(columnIterator, config, errorList)) hasErrorAllFile = true;
            }

        }
        if (hasErrorAllFile) {
            System.out.print("Есть ошибки, смотри примечания на ячейках в исходном файле Excel. Идет запись:");
            for (ErrorList error : errorLists) {
                try {
                    adapterExcel.writeDataToExcel(error);
                } catch (IOException e) {
                    System.out.println("Файл " + excelFileFullPathName + " не доступен (отсутствует или занят/открыт в данный момент");
                }

            }
            System.out.println(" ОК");
        }
    }
}
