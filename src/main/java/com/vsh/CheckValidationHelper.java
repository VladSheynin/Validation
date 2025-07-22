package com.vsh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheckValidationHelper {
    static List<ConfigObjectForValidation> configsList;

    //получаю все объекты валидации из метода
    public CheckValidationHelper() {
        configsList = getAllConfigObject();
    }

    //TODO:получение все объекты валидации из фалйа
    public CheckValidationHelper(File file) {
        //получить строку json из файла и создать объект
    }

    //TODO:получение все объекты валидации из json-а
    public CheckValidationHelper(String jsonString) {
        //jsonString.structureValidation();
    }

    //тестовое наполнение конфигурацией - переделать на получение из json или базы
    public static List<ConfigObjectForValidation> getAllConfigObject() {
        List<ConfigObjectForValidation> configsList = new ArrayList<>();
        //Тестовые данные для INN
        RegularsWithStrings regular = new RegularsWithStrings("^\\d{9}$", "Ошибка: в ИНН не только цифры или их количество не 9", "Проверяю что в поле ИНН только цифры и их ровно 9");
        RegularsWithStrings regular1 = new RegularsWithStrings("^123.*", "Ошибка: ИНН начинается не с 123", "Проверяю на то что значение ИНН начинается с 123");
        List<RegularsWithStrings> regularsList = new ArrayList<>();
        //regularsList.add(regular);
        //regularsList.add(regular1);
        configsList.add(new ConfigObjectForValidation("INN", false, false, regularsList));

        //Тестовые данные 2 для Account
        RegularsWithStrings regular2 = new RegularsWithStrings("^\\d{20}$", "Ошибка: в поле не только цифры или их количество не 20", "Ошибка: в поле не только цифры или их количество не 20");
        RegularsWithStrings regular3 = new RegularsWithStrings("^408.*", "Ошибка: номер счета начинается не с 408", "Проверяю на то что значение начинается с 408");
        List<RegularsWithStrings> regularsList2 = new ArrayList<>();
        regularsList2.add(regular2);
        regularsList2.add(regular3);
        configsList.add(new ConfigObjectForValidation("account-false", true, true, regularsList2));
        return configsList;
    }

    /**
     * Поиск тестового оъекта для столбца и отправку, если таковой найден, столбца на проерки
     *
     * @param columnName - строка с именем столбца
     * @return - объект конфигурации если найден, иначе null
     */
    public static ConfigObjectForValidation getConfigurationObject(String columnName) {
        ConfigObjectForValidation config = findChecker(columnName.trim()); // ищем тестовый ConfigObject по имени столбца
        if (config == null) {
            //System.out.println("Конфигурационный объект для " + columnName + " не найден");
            return null;
        } else {
            //System.out.println("Конфигурационный объект для " + columnName + " найден");
            return config;
        }
    }

    /**
     * Проведение проверок, проверяет какие типы проверок запускать
     *
     * @param column    - список элементов для проверки
     * @param config    - объет с элементами проверок
     * @param errorList - список ошибок
     * @return true - если ошибок нет, false - если ошибки есть
     */
    public static boolean validation(List<ObjectForValidation> column, ConfigObjectForValidation config, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        if (config.getIsNotEmpty()) {
            if (!checkIsEmpty(column, errorList)) {
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + " на не пустые ячейки");
                returnSuccessFlag = false;
            }
        }else System.out.println("Отсутствует проверка на пустые поля");
        if (config.getIsUnique()) {
            if (!checkIsUnique(column, errorList)) {
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + " на уникальность данных по столбцу");
                returnSuccessFlag = false;
            }
        }else System.out.println("Отсутствует проверка уникальность значений");
        if (!config.getRegulars().isEmpty()) {
            if (!checkByRegulars(column, config, errorList)) {
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + " на маски (регулярные выражения");
                returnSuccessFlag = false;
            }
        } else System.out.println("Отсутствуют проверки по маске (регулярные выражения)");

        return returnSuccessFlag;
    }

    /**
     * Проверка ячеек по списку регулярных выражений
     *
     * @param column    - список элементов для проверки
     * @param config    - объект конфигурации
     * @param errorList - список ошибок
     * @return true - если значение ячейки удовлетворяет маске регулярки, false - если не удовлетворяет
     * добавляет строки с ошибками в errorList
     */
    public static boolean checkByRegulars(List<ObjectForValidation> column, ConfigObjectForValidation config, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        List<RegularsWithStrings> regulars = config.getRegulars();
        ObjectForValidation object;
        for (int i = 1; i < column.size(); i++) {//нулевой элемент - заголовок
            object = column.get(i);
            for (RegularsWithStrings regular : regulars) {
                System.out.print("строка " + object.getRawExcel() + ": " + regular.getWorkingMessage() + " - ");
                if (!checkOneRegularByStrings(object.getDataForCheck(), regular.getRegular())) {
                    System.out.println(regular.getErrorMessage());
                    errorList.addErrorToList(new ErrorObject(object.getRawExcel(), object.getColumnExcel(), regular.getErrorMessage()));
                } else System.out.println("OK");
            }
        }
        return returnSuccessFlag;
    }

    /**
     * Проверка значения в ячейке по конкретной регулярке
     *
     * @param objectString  - строка со значением ячейки
     * @param regularString - строка с регулярным выражением
     * @return true - значение ячейки соответствует регулярке, false - не соответствует
     */
    public static boolean checkOneRegularByStrings(String objectString, String regularString) {
        return objectString.matches(regularString);
    }

    /**
     * Проверка на то что ячейка не должна быть пустой
     *
     * @param column    - список элементов для проверки
     * @param errorList - список ошибок
     * @return true - если нет пустых, false - если есть пустые.
     * добавляет строки с ошибками в errorList
     */
    public static boolean checkIsEmpty(List<ObjectForValidation> column, ErrorList errorList) {
        boolean returnSuccessFlag = true;

        for (ObjectForValidation object : column) {
            if (object.getDataForCheck().isEmpty()) {
                System.out.println("строка " + object.getRawExcel() + ": поле не может быть пустым");
                errorList.addErrorToList(new ErrorObject(object.getRawExcel(), object.getColumnExcel(), "Поле не может быть пустым"));
                returnSuccessFlag = false;
            }
        }
        return returnSuccessFlag;
    }

    /**
     * Проверка на то что ячейки уникальны
     *
     * @param column    - список элементов для проверки
     * @param errorList - список ошибок
     * @return true - если ячейки не повторяются, false - если есть повторения
     * добавляет строки с ошибками в errorList
     */
    public static boolean checkIsUnique(List<ObjectForValidation> column, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        String errorString = "";
        boolean notUniqueflag = false;
        for (int i = 1; i < column.size(); i++) {//от 1 так как нулевой элемент - заголовок
            errorString = "Ошибка проверки на уникальность: дублируются строки " + column.get(i).getRawExcel() + " (" + column.get(i).getDataForCheck() + ")";
            for (int j = 1; j < column.size(); j++) {
                if (i == j) continue; //убираю сравнение самого с собой
                notUniqueflag = false;
                if (Objects.equals(column.get(i).getDataForCheck(), column.get(j).getDataForCheck())) {
                    errorString = errorString + " и " + column.get(j).getRawExcel() + " (" + column.get(j).getDataForCheck() + ")";
                    notUniqueflag = true;
                }
                if (notUniqueflag) {
                    errorList.addErrorToList(new ErrorObject(column.get(i).getRawExcel(), column.get(i).getColumnExcel(), errorString));
                    returnSuccessFlag = false;
                }
            }
        }
        return returnSuccessFlag;
    }

    /**
     * Поиск и возврат объект конфигурации по имени поля
     *
     * @param fieldName - название столбца (идентификатор объекта)
     * @return объект конфигурации - если найден, null - если такой объект отсутствует
     * добавляет строки с ошибками в errorList
     */
    public static ConfigObjectForValidation findChecker(String fieldName) {
        for (ConfigObjectForValidation config : configsList) {
            if (Objects.equals(config.getColumnNameID().trim().toUpperCase(), fieldName.trim().toUpperCase())) {
                //System.out.println("findChecker: Объект для проверки найден (" + fieldName + ")");
                return config;
            }
        }
        //System.out.println("findChecker: Объект для проверки не найден (" + fieldName + ")");
        return null;
    }
}
