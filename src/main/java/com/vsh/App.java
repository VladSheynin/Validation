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

    static List<ErrorList> errorLists = new ArrayList<>();
    static final CheckValidationHelper helper = new CheckValidationHelper();

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
        int testColumn = 0;
        String fieldName = "";
        ErrorList errorList = new ErrorList();
        for (int j = 0; j < allDataFromExcel.get(0).size(); j++) {
            columnIterator.clear();
            for (List<ObjectForValidation> object : allDataFromExcel) {
                columnIterator.add(object.get(j));
            }

            //!!!не забыть создать новый errorList - он будет заполнен после каждого столбца
            errorList = new ErrorList(columnIterator.get(0).getDataForCheck());
            errorLists.add(errorList);    //создаю отдельные очереди для каждого столбца,
            // для возможности расспараллелить проверки
            if (!CheckValidationHelper.check(columnIterator, errorList)) {
                System.out.println("Найдены ошибки в столбце " + columnIterator.get(0).getDataForCheck());
            } else {
                System.out.println("Проверка по столбцу " + columnIterator.get(0).getDataForCheck() + " успешно пройдена");
            }
        }
        for(ErrorList error:errorLists){
            System.out.println(error.toString());
        }
    }


}
