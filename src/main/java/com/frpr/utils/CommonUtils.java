package com.frpr.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class CommonUtils {

    public static Date setEndOfDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 900);
        return calendar.getTime();
    }

    public static Date formatDate(DateFormatEnum dateFormatEnum, String dateString) {
        if (dateFormatEnum == null) {
            throw new RuntimeException("Invalid dateformat enum");
        }

        if (StringUtils.isBlank(dateString)) {
            throw new RuntimeException(String.format("Invalid date string. date format should be %s", dateFormatEnum.getValue()));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatEnum.getValue());

        try {
            return simpleDateFormat.parse(dateString);
        } catch (Exception e) {
            log.error("CommonUtils#formatDate", e);
            throw new RuntimeException(String.format("Invalid date format %s. date format should be %s", dateString, dateFormatEnum.getValue()));
        }
    }
}
