package com.vsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.vsh.AdapterExcel.getDataFromExcel;
import static com.vsh.AdapterExcel.writeDataToExcel;

/**
 * Базовый класс
 */
public class App {

    static final CheckValidationHelper helper = new CheckValidationHelper();
    static List<ErrorList> errorLists = new ArrayList<>();

    public static void main(String[] args) {
        String fileFullPathName = "C:\\Projects\\Validation\\example.xlsx";
        List<List<ObjectForValidation>> allDataFromExcel = new ArrayList<>();
        try {
            allDataFromExcel = getDataFromExcel(fileFullPathName);
            if (allDataFromExcel == null) {
                System.out.println("Данные не разобраны");
                return;
            }
        } catch (IOException e) {
            System.out.println("Файл " + fileFullPathName + " не найден");
        }

        //обработка testColumn-того столбца
        List<ObjectForValidation> columnIterator = new ArrayList<>();
        ErrorList errorList;
        for (int j = 0; j < allDataFromExcel.get(0).size(); j++) {
            columnIterator.clear();
            for (List<ObjectForValidation> object : allDataFromExcel) {
                columnIterator.add(object.get(j));
            }

            errorList = new ErrorList("Столбец " + columnIterator.get(0).getDataForCheck());
            errorLists.add(errorList);    //создаю отдельные очереди для каждого столбца,
            ConfigObjectForValidation config; //объект конфигурации
            //TODO: распараллелить проверки по отдельным потокам

            // получаем объект конфигурации и стартуем с ним проверки
            config = CheckValidationHelper.getConfigurationObject(columnIterator.get(0).getDataForCheck());
            if (config == null) {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " не найден");
            } else {
                System.out.println("Объект конфигурации для " + columnIterator.get(0).getDataForCheck() + " найден. Стартую проверки");
                CheckValidationHelper.validation(columnIterator, config, errorList);
            }

        }
        for (ErrorList error : errorLists) {
            try {
                writeDataToExcel(fileFullPathName,error);
            } catch (IOException e) {
                System.out.println("Файл " + fileFullPathName + " не доступен (отсутствует или занят/открыт в данный момент");
            }
            //System.out.println(error.toString());
        }
    }
}
