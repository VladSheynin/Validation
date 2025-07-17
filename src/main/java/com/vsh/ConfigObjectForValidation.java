package com.vsh;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект, который содержит все необходимые проверки
 *
 * @author Владислав Шейнин (начало разработки 17.07.2025)
 * @version 0.1
 */
public class ConfigObjectForValidation {
    String columnNameID;
    boolean isNotEmpty;
    boolean isUnique;
    List<RegularsWithStrings> regulars = new ArrayList<>();

    public ConfigObjectForValidation(String columnNameID, boolean isNotEmpty, boolean isUnique, List<RegularsWithStrings> regulars) {
        this.columnNameID = columnNameID;
        this.isNotEmpty = isNotEmpty;
        this.isUnique = isUnique;
        this.regulars = regulars;
    }

    public String getColumnNameID() {
        return columnNameID;
    }

    public boolean isNotEmpty() {
        return isNotEmpty;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public List<RegularsWithStrings> getRegulars() {
        return regulars;
    }

    /**
     * ToDo: проверка что полученные данные соответствуют формату объекта
     *
     * @return
     */
    public boolean validationFormatCheck() {

        return true;
    }

}
