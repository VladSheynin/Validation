package com.vsh;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.File;
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

        CheckValidationHelper helper = new CheckValidationHelper();
        if (!helper.setFile(new File(configsFileFullPathName))) {
            //ошибка, сообщение об ошибке - в методе соответствующего класса
            return;
        }
        helper.getConfigObjectsFromFile();

        AdapterExcel adapterExcel = new AdapterExcel(color, type);
        if (!adapterExcel.setFile(new File(excelFileFullPathName))) {
            //ошибка, сообщение об ошибке - в методе соответствующего класса
            return;
        }

        // получаем все данные из Excel
        List<List<ObjectForValidation>> allDataFromExcel = getAllDataFromExcell(adapterExcel);
        if (allDataFromExcel == null)
            return; //выход если файл разобрать не удалось. Ошибки логируются в соответствующих методах


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

    private static List<List<ObjectForValidation>> getAllDataFromExcell(AdapterExcel adapterExcel) {
        List<List<ObjectForValidation>> allDataFromExcel;
        allDataFromExcel = adapterExcel.readFromExcel();
        if (allDataFromExcel == null) {
            System.out.println("Данные не разобраны.");
            return null;
        } else return allDataFromExcel;
    }
}
