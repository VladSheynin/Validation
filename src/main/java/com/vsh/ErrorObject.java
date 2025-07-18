package com.vsh;

public class ErrorObject {
    private final int rowExcel;
    private final int columnExcel;
    private final String errorMessage;

    public ErrorObject(int rowExcel, int columnExcel, String errorMessage) {
        this.rowExcel = rowExcel;
        this.columnExcel = columnExcel;
        this.errorMessage = errorMessage;
    }

    public int getRowExcel() {
        return rowExcel;
    }

    public int getColumnExcel() {
        return columnExcel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
