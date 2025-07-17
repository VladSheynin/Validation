package com.vsh;

/**
 * Объект, который требуется проверить
 * @author Владислав Шейнин (начало разработки 17.07.2025)
 * @version 0.1
 */
public class ObjectForValidation {
    String dataForCheck;
    int rawExcel;
    int columnExcel;
    public ObjectForValidation(String dataForCheck, int rawExcel, int columnExcel){
        this.dataForCheck = dataForCheck;
        this.rawExcel = rawExcel;
        this.columnExcel = columnExcel;
    }

    public String getDataForCheck() {
        return dataForCheck;
    }

    public int getRawExcel() {
        return rawExcel;
    }

    public int getColumnExcel() {
        return columnExcel;
    }
}
