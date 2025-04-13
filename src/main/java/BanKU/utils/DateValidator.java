package BanKU.utils;

import java.time.DateTimeException;
import java.time.MonthDay;

public class DateValidator {
    public static MonthDay validateDate(String rawDate) throws DateTimeException {
        String date = rawDate.trim();
        // 숫자 형식은 맞지만 자릿수가 잘못됨 (예: 6자리, 3자리 등)
        if (date.matches("\\d+") && !date.matches("\\d{4}")) {
            throw new IllegalArgumentException("[ERROR] 4자리 숫자를 입력해주세요.");
        }

        // 숫자 외의 문자가 포함되어 있는 경우
        if (!date.matches("\\d{4}")) {
            throw new IllegalArgumentException("[ERROR] 올바른 날짜 형식으로 입력해주세요.");
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
