package com.vsh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.vsh.AdapterExcel.getDataFromExcel;

public class CheckValidationHelper {
    static List<ConfigObjectForValidation> configsList;

    //получаю все объекты валидации из метода
    public CheckValidationHelper() {
        configsList = getAllConfigObject();
    }

    //TODO:получение все объекты валидации из фалйа
    public CheckValidationHelper(File file)
    {
        //получить строку json из файла и создать объект
    }

    //TODO:получение все объекты валидации из json-а
    public CheckValidationHelper(String jsonString)
    {
        //jsonString.structureValidation();
    }

    //тестовое наполнение конфигурацией - переделать на получение из json или базы
    public static List<ConfigObjectForValidation> getAllConfigObject() {
        List<ConfigObjectForValidation> configsList = new ArrayList<>();
        //Тестовые данные для INN
        RegularsWithStrings regular = new RegularsWithStrings("^\\d{9}$", "Ошибка: в поле не только цифры или их количество не 9", "Проверяю что в поле только цифры");
        List<RegularsWithStrings> regularsList = new ArrayList<>();
        regularsList.add(regular);
        configsList.add(new ConfigObjectForValidation("INN", true, true, regularsList));

        //Тестовые данные 2 для Account
        RegularsWithStrings regular2 = new RegularsWithStrings("^\\d{20}$", "Ошибка: в поле не только цифры или их количество не 20", "Ошибка: в поле не только цифры или их количество не 20");
        RegularsWithStrings regular3 = new RegularsWithStrings("^408.*", "Ошибка: номер счета начинается не с 408", "Проверяю на то что значение начинается с 408");
        List<RegularsWithStrings> regularsList2 = new ArrayList<>();
        regularsList.add(regular2);
        regularsList.add(regular3);
        configsList.add(new ConfigObjectForValidation("ACCOUNT", false, true, regularsList));
        return configsList;
    }

    public static boolean check(List<ObjectForValidation> columnObjects, ErrorList errorList) {
        ConfigObjectForValidation config = findChecker(columnObjects.get(0).getDataForCheck().trim()); // ищем тестовый ConfigObject по имени столбца
        if (config == null) {
            System.out.println("check: Конфигурационный объект для " + columnObjects.get(0).getDataForCheck().trim() + " не найден");
            return false;
        } else {
            System.out.println("check: Объект для проверки найден");
            if (validation(columnObjects, config, errorList)) {
                System.out.println("check: Проверки пройдены успешно");
                return true;
            } else {
                System.out.println("check: Проверки не пройдены");
                return false;
            }
        }
    }

    //фактическое проведение проверок (вынести в отдельный класс проверок
    public static boolean validation(List<ObjectForValidation> column, ConfigObjectForValidation config, ErrorList errorList) {
        if (config.getIsNotEmpty()) {
            if (!checkIsEmpty(column, errorList))
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + "на не пустые ячейки");
        }
        if (config.getIsUnique()) {
            if (!checkIsUnique(column, errorList))
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + "на уникальность данных по столбцу");
        }

        //checkByRegulars(column, config);

        return false;
    }

    public static boolean checkIsEmpty(List<ObjectForValidation> column, ErrorList errorList) {
        boolean returnSuccessFlag = true;

        for (ObjectForValidation object : column) {
            if (object.getDataForCheck().isEmpty()) {

                errorList.addErrorToList(new ErrorObject(object.getRawExcel(), object.getColumnExcel(), "Поле не может быть пустым"));
                returnSuccessFlag = false;
            }
        }
        return returnSuccessFlag;
    }

    public static boolean checkIsUnique(List<ObjectForValidation> column, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        String errorString = "";
        boolean notUniqueflag = false;
        for (int i = 1; i < column.size(); i++) {//от 1 так как первый элемент - заголовок
            errorString = "Ошибка проверки на уникальность: дублируются строки " + column.get(i).getRawExcel() + " (" + column.get(i).getDataForCheck() + ")";
            for (int j = 1; j < column.size(); j++) {
                if (i == j) continue; //убираю сравнение самого с собой
                notUniqueflag = false;

                if (Objects.equals(column.get(i).getDataForCheck(), column.get(j).getDataForCheck())) {
                    errorString = errorString + " и " + column.get(j).getRawExcel() + " (" + column.get(j).getDataForCheck() + ")";
                    notUniqueflag = true;
                }
            }
            if (notUniqueflag) {
                errorList.addErrorToList(new ErrorObject(column.get(i).getRawExcel(), column.get(i).getColumnExcel(), errorString));
                returnSuccessFlag = false;
            }
        }
        return returnSuccessFlag;
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
}
