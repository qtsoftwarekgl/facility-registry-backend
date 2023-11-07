package com.frpr.utils;
import java.util.Random;

public class GenerateOTPUtils {
    public static String generateOTP(){
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000) );
        return otp;
    }
}
