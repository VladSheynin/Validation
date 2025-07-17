package com.vsh;

/**
 * Объект, который содержит регулярное выражение и сообщения по его обработке
 *
 * @author Владислав Шейнин (начало разработки 18.07.2025)
 * @version 0.1
 */
public class RegularsWithStrings {
    String regular;
    String errorMessage;
    String workingMessage;

    public RegularsWithStrings(String regular, String errorMessage, String workingMessage) {
        this.regular = regular;
        this.errorMessage = errorMessage;
        this.workingMessage = workingMessage;
    }
}
