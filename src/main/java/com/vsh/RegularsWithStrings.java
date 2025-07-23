package com.vsh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Объект, который содержит регулярное выражение и сообщения для логирования и описания ошибки
 * @author Владислав Шейнин (начало разработки 18.07.2025)
 * @version 0.1
 */
public class RegularsWithStrings implements Serializable {
    private String regular;
    private String errorMessage;
    private String workingMessage;

    @JsonCreator
    public RegularsWithStrings(
            @JsonProperty("regular") String regular,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("workingMessage") String workingMessage) {
        this.regular = regular;
        this.errorMessage = errorMessage;
        this.workingMessage = workingMessage;
    }

    public String getRegular() {
        return regular;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getWorkingMessage() {
        return workingMessage;
    }
}
