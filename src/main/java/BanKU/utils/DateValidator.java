package BanKU.utils;

import java.time.DateTimeException;
import java.time.MonthDay;

public class DateValidator {
    public static MonthDay validateDate(String rawDate) throws DateTimeException {
        String date = rawDate.trim();
        boolean isFourDigits = date.matches("\\d{4}");
        if (!isFourDigits) {
            throw new IllegalArgumentException("[ERROR] 4자리 숫자를 입력해주세요.");
        }
        int month = Integer.parseInt(date.substring(0, 2));
        int day = Integer.parseInt(date.substring(2, 4));
        try {
            MonthDay monthDay = MonthDay.of(month, day);
            if (monthDay.equals(MonthDay.of(2, 29))) {
                throw new DateTimeException("[ERROR] 윤년은 무시합니다.");
            }
            return monthDay;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("[ERROR] 올바른 날짜 형식으로 입력해주세요.");
        }
    }
}
