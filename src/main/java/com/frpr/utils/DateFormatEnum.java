package com.frpr.utils;

import lombok.Getter;

@Getter
public enum DateFormatEnum {

    DDMMYYYY("dd/MM/yyyy"),
    YYYYMMDD("yyyy-MM-dd");

    private final String value;

    DateFormatEnum(String value) {
        this.value = value;
    }
}
