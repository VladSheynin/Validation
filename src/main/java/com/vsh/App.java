package com.vsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.vsh.AdapterExcel.getDataFromExcel;

/**
 * Базовый класс
 */
public class App {
    static List<ConfigObjectForValidation> configsList;

    public static void main(String[] args) {
        String fileFullPathName = "C:\\Projects\\Validation\\example.xlsx";
        List<List<String>> allDataFromExcel = new ArrayList<>();
        configsList = getAllConfigObject();
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
        List<String> columnIterator = new ArrayList<>();
        int testColumn = 0;
        String fieldName = "";
        for (int j = 0; j < allDataFromExcel.get(0).size(); j++) {
            columnIterator.clear();
            for (List<String> strings : allDataFromExcel) {
                columnIterator.add(strings.get(j));
            }
            if (!check(columnIterator)) {
                System.out.println("Найдены ошибки в столбце " + columnIterator.get(0));
            } else {
                System.out.println("Проверка по столбцу " + columnIterator.get(0) + " успешно пройдена");
            }
        }
    }

    public static boolean check(List<String> columnObjects) {
        ConfigObjectForValidation config = findChecker(columnObjects.get(0).trim()); // ищем тестовый ConfigObject по имени столбца
        if (config == null) {
            System.out.println("check: Конфигурационный объект для " + columnObjects.get(0).trim() + " не найден");
            return false;
        } else {
            System.out.println("check: Объект для проверки найден");
            if (validation(columnObjects, config)) {
                System.out.println("check: Проверки пройдены успешно");
                return true;
            } else {
                System.out.println("check: Проверки не пройдены");
                return false;
            }
        }
    }

    //фактическое проведение проверок (вынести в отдельный класс проверок
    public static boolean validation(List<String> column, ConfigObjectForValidation config) {
        checkIsEmpty(column);
        checkIsUnique(column);
        checkByRegulars(column,config);

        return false;
    }

    public static ConfigObjectForValidation findChecker(String fieldName) {
        for (ConfigObjectForValidation config : configsList) {
            if (Objects.equals(config.getColumnNameID(), fieldName)) {
                System.out.println("findChecker: Объект для проверки найден");
                return config;
            }
        }
        return null;
    }

    //тестовое наполнение конфигурацией - переделать на получение из json или базы
    public static List<ConfigObjectForValidation> getAllConfigObject() {
        List<ConfigObjectForValidation> configsList = new ArrayList<>();
        //получить из json
        //Тестовые данные
        RegularsWithStrings regular = new RegularsWithStrings("^\\d{9}$", "Ошибка: в поле не только цифры или их количество не 9", "Проверяю что в поле только цифры");
        List<RegularsWithStrings> regularsList = new ArrayList<>();
        regularsList.add(regular);
        configsList.add(new ConfigObjectForValidation("INN", false, true, regularsList));

        //Тестовые данные 2
        RegularsWithStrings regular2 = new RegularsWithStrings("^\\d{20}$", "Ошибка: в поле не только цифры или их количество не 20", "Ошибка: в поле не только цифры или их количество не 20");
        RegularsWithStrings regular3 = new RegularsWithStrings("^408.*", "Ошибка: номер счета начинается не с 408", "Проверяю на то что значение начинается с 408");
        List<RegularsWithStrings> regularsList2 = new ArrayList<>();
        regularsList.add(regular2);
        regularsList.add(regular3);
        configsList.add(new ConfigObjectForValidation("ACCOUNT", false, false, regularsList));
        return configsList;
    }
}
