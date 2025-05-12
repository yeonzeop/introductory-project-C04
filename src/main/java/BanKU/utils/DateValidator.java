package BanKU.utils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoField;

public class DateValidator {
    public static LocalDate validateDate(String rawDate) {
        String date = rawDate.trim();
        // 숫자 형식은 맞지만 자릿수가 잘못됨 (예: 4자리, 3자리 등)
        if (date.matches("\\d+") && !date.matches("\\d{6}")) {
            throw new IllegalArgumentException("[ERROR] 6자리 숫자를 입력해주세요.");
        }

        // 숫자 외의 문자가 포함되어 있는 경우
        if (!date.matches("\\d{6}")) {
            throw new IllegalArgumentException("[ERROR] 올바른 날짜 형식으로 입력해주세요.");
        }
        int year = Integer.parseInt(date.substring(0,2)) + 2000;
        int month = Integer.parseInt(date.substring(2, 4));
        int day = Integer.parseInt(date.substring(4, 6));
        try {
            LocalDate localDate = LocalDate.of(year,month, day);
            if(localDate.get(ChronoField.YEAR)<=2024){
                throw new IllegalArgumentException("[ERROR] 2025년 이전 연도는 서비스 대상이 아닙니다.");
            }
            return localDate;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("[ERROR] 올바른 날짜 형식으로 입력해주세요.");
        }
    }
}
