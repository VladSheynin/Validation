package com.vsh;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Класс получения всех объектов валидации из фалйа
 *
 * @author Владислав Шейнин (начало разработки 18.07.2025)
 * @version 0.1
 */
public class CheckValidationHelper {
    private List<ConfigObjectForValidation> configsList;

    public CheckValidationHelper(File file) throws FileNotFoundException {
        if (!checkFile(file)) throw new FileNotFoundException();
        else try {
            getConfigObjectsFromFile(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new FileNotFoundException();
        }
    }

    /**
     * Метод получающий объекты из json-файла и складывающий их в List<ConfigObjectForValidation> configsList
     *
     * @param file - файл
     */
    private void getConfigObjectsFromFile(File file) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.print("Читаю конфигурацию из файла " + file.getAbsolutePath() + " : ");
        try {
            configsList = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, ConfigObjectForValidation.class));
        } catch (IOException e) {
            throw new Exception("Файл " + file.getAbsolutePath() + " не является правильным файлом конфигурации или файл заблокирован");
        }
        System.out.println("OK");
    }

    /**
     * Поиск тестового объекта для столбца и отправку, если таковой найден, столбца на проерки
     *
     * @param columnName - строка с именем столбца
     * @return - объект конфигурации если найден, иначе null
     */
    public ConfigObjectForValidation getConfigurationObject(String columnName) {
        return findChecker(columnName.trim());
    }

    /**
     * Проведение проверок, проверяет какие типы проверок запускать
     *
     * @param column    - список элементов для проверки
     * @param config    - объет с элементами проверок
     * @param errorList - список ошибок
     * @return true - если ошибок нет, false - если ошибки есть
     */
    public boolean validation(List<ObjectForValidation> column, ConfigObjectForValidation config, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        if (config.isNotEmpty()) {
            if (!checkIsEmpty(column, errorList)) {
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + " на не пустые ячейки");
                returnSuccessFlag = false;
            }
        } else System.out.println("Отсутствует проверка на пустые поля");
        if (config.isUnique()) {
            if (!checkIsUnique(column, errorList)) {
                System.out.println("Ошибка при проверке столбца " + column.get(0).getDataForCheck() + " на уникальность данных по столбцу");
                returnSuccessFlag = false;
            }
        } else System.out.println("Отсутствует проверка уникальность значений");
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
    public boolean checkByRegulars(List<ObjectForValidation> column, ConfigObjectForValidation config, ErrorList errorList) {
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
                    returnSuccessFlag = false;
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
    public boolean checkOneRegularByStrings(String objectString, String regularString) {
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
    public boolean checkIsEmpty(List<ObjectForValidation> column, ErrorList errorList) {
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
    public boolean checkIsUnique(List<ObjectForValidation> column, ErrorList errorList) {
        boolean returnSuccessFlag = true;
        String errorString;
        boolean notUniqueflag;
        for (int i = 1; i < column.size(); i++) {//от 1 так как нулевой элемент - заголовок
            errorString = "Ошибка проверки на уникальность: дублируются строки " + (column.get(i).getRawExcel() + 1) + " (" + column.get(i).getDataForCheck() + ")";
            notUniqueflag = false;
            for (int j = 1; j < column.size(); j++) {
                if (i == j) continue; //убираю сравнение самого с собой

                if (Objects.equals(column.get(i).getDataForCheck(), column.get(j).getDataForCheck())) {
                    errorString = errorString + " и " + (column.get(j).getRawExcel() + 1) + " (" + column.get(j).getDataForCheck() + ")";
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

    /**
     * Поиск и возврат объект конфигурации по имени поля
     *
     * @param fieldName - название столбца (идентификатор объекта)
     * @return объект конфигурации - если найден, null - если такой объект отсутствует
     * добавляет строки с ошибками в errorList
     */
    public ConfigObjectForValidation findChecker(String fieldName) {
        for (ConfigObjectForValidation config : configsList) {
            if (Objects.equals(config.getColumnNameID().trim().toUpperCase(), fieldName.trim().toUpperCase())) {
                //System.out.println("findChecker: Объект для проверки найден (" + fieldName + ")");
                return config;
            }
        }
        //System.out.println("findChecker: Объект для проверки не найден (" + fieldName + ")");
        return null;
    }

    /**
     * Проверка, что файл существует и можно работать дальше
     *
     * @param file - файл
     * @return true - существует, false - не существует
     */
    private boolean checkFile(File file) {
        if (!file.exists()) {
            System.out.println("Файл " + file.getAbsolutePath() + " не существует");
            return false;
        }
        return true;
    }
}
