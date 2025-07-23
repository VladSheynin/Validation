package com.vsh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.vsh.AdapterExcel.getDataFromExcel;
import static com.vsh.AdapterExcel.writeDataToExcel;

/**
 * Базовый класс
 */
public class App {

    private static final String excelFileFullPathName = "C:\\Projects\\Validation\\example.xlsx";
    private static final String configsFileFullPathName = "C:\\Projects\\Validation\\testConfig.json";
    static List<ErrorList> errorLists = new ArrayList<>();

    public static void main(String[] args) {
        File file = new File(configsFileFullPathName);
        if (!file.exists()) {
            System.out.println("Файл " + file.getAbsolutePath() + " не существует");
            return;
        }
        CheckValidationHelper helper = new CheckValidationHelper(file);

        File fileExcel = new File(excelFileFullPathName);
        if (!fileExcel.exists()) {
            System.out.println("Файл " + fileExcel.getAbsolutePath() + " не существует");
            return;
        }
        List<List<ObjectForValidation>> allDataFromExcel = new ArrayList<>();
        try {
            allDataFromExcel = getDataFromExcel(fileExcel);
            if (allDataFromExcel == null) {
                System.out.println("Данные не разобраны");
                return;
            }
        } catch (IOException e) {
            System.out.println("Файл " + excelFileFullPathName + " не найден");
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
            //ConfigObjectForValidation config; //объект конфигурации
            //TODO: распараллелить проверки по отдельным потокам

            // получаем объект конфигурации и стартуем с ним проверки
            ConfigObjectForValidation config = CheckValidationHelper.getConfigurationObject(columnIterator.get(0).getDataForCheck());
            if (config == null) {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " не найден");
            } else {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " найден. Стартую проверки");
                if (!CheckValidationHelper.validation(columnIterator, config, errorList)) hasErrorAllFile = true;
            }

        }
        if (hasErrorAllFile) {
            System.out.println("Есть ошибки, смотри примечания на ячейках в исходном файле Excel");
            for (ErrorList error : errorLists) {
                try {
                    writeDataToExcel(fileExcel, error);
                } catch (IOException e) {
                    System.out.println("Файл " + excelFileFullPathName + " не доступен (отсутствует или занят/открыт в данный момент");
                }

            }
        }
    }
}
