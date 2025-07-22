package com.vsh;

import java.util.ArrayList;
import java.util.List;

/**
 * Очереди сообщений об ошибках
 *
 * @author Владислав Шейнин (начало разработки 18.07.2025)
 * @version 0.1
 */
public class ErrorList {
    private final List<ErrorObject> errorObjectList = new ArrayList<>();
    private String errorListName = "";

    public ErrorList() {
    }

    public ErrorList(String name) {
        this.errorListName = name;
    }

    public void addErrorToList(ErrorObject object) {
        errorObjectList.add(object);
    }

    public List<ErrorObject> getAllErrorObject() {
        return errorObjectList;
    }

    @Override
    public String toString() {
        String string;

        if (errorListName.isEmpty())
            string = "ErrorList:";
        else
            string = this.errorListName + ": ";

        for (ErrorObject object : errorObjectList) {
            string = string + "\n в ячейке (" + object.getRowExcel() + "," + object.getColumnExcel() + ") " + object.getErrorMessage();
        }
        return string;
    }
}
