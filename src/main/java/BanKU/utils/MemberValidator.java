package BanKU.utils;

import BanKU.domain.Account;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class MemberValidator {

    /**
     * 로그인 아이디 = 영어 + 숫자 최대 10 자 (영어 알파벳 대소문자 구분 없음)
     * - 아이디 길이는 2글자 이상 10글자 이하여야 한다.
     * - 아이디에 한글 또는 특수문자가 포함되면 안된다.
     */
    public static String validateLoginId(String rawLoginId) {
        String loginId = rawLoginId.trim().toUpperCase();
        boolean hasKorean = loginId.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
        boolean hasSpecial = loginId.matches(".*[^a-zA-Z0-9].*");
        if (2 <= loginId.length() && loginId.length() <= 10 && !hasKorean && !hasSpecial) {
            return loginId;
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 로그인 아이디 값이 형식에 맞지 않습니다.");
    }

    /**
     * 로그인 비밀번호 = 숫자 6자리
     * - 비밀번호는 6자리여야 한다.
     * - 숫자로만 이루어져야 한다.
     */
    public static String validatePassword(String rawPassword) {
        String password = rawPassword.trim();
        boolean isSixDigitNumber = password.matches("\\d{6}");
        if (isSixDigitNumber) {
            return password;
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 로그인 비밀번호가 형식에 맞지 않습니다.");
    }


    /**
     * 이름 = 한글 최대 2 글자 이상 4 글자 이하 (이름은 반드시 한글만 입력 가능)
     * - 이름에 영어, 숫자, 특수 문자가 포함되면 안됨
     * - 한글이 한 글자 이하 또는 5 글자 이상이면 안됨
     */
    public static String validateName(String rawName) {
        String name = rawName.trim();
        boolean isValidName = name.matches("^[가-힣]{2,4}$");
        if (isValidName) {
            return name;
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 이름이 형식에 맞지 않습니다.");
    }

    /**
     * YYMMDD 형식의 날짜 입력
     * - 존재하지 않는 날짜인 경우
     * - 은행 서비스 이용가능 연령에 부합되지 않는 경우 : 1926 년부터 2011 년 사이에 출생한 사용자만 이용 가능
     */
    public static LocalDate validateBirthday(String rawBirthday) {
        String birthday = rawBirthday.trim();
        boolean isSixDigitNumber = birthday.matches("\\d{6}");
        if (!isSixDigitNumber) {
            throw new IllegalArgumentException("[ERROR] 사용자의 생일이 숫자로만 이루어져 있지 않습니다.");
        }
        String fullBirthday = validateMemberAge(birthday);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            return LocalDate.parse(fullBirthday, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 날짜입니다.");
        }
    }

    private static String validateMemberAge(String birthday) {
        int yearSuffix = Integer.parseInt(birthday.substring(0, 2));
        boolean isInvalidRange = (yearSuffix >= 12 && yearSuffix <= 25);
        if (isInvalidRange) {
            throw new IllegalArgumentException("[ERROR] 사용자는 은행 서비스를 이용할 수 없는 연령입니다. 출생년도 = " + yearSuffix);
        }
        String centuryPrefix = (yearSuffix <= 11) ? "20" : "19";
        return centuryPrefix + birthday;
    }

    /**
     * 전화번호 = 010 을 제외한 뒷 번호 8 자리
     * - 숫자가 8 자리가 아닌 경우
     * - 숫자가 아닌 문자들이 섞여 있는 경우
     */
    public static String validatePhoneNumber(String rawPhoneNumber) {
        String phoneNumber = rawPhoneNumber.trim();
        boolean isEightDigitNumber = phoneNumber.matches("\\d{8}");
        if (isEightDigitNumber) {
            return phoneNumber;
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 전화번호가 형식에 맞지 않습니다.");
    }


    // ex. 계좌 데이터 예시 : 123456789012 & 1234  %  210987654321 & 4322  %  564738291027 & 6342
    public static List<Account> createAccounts(String rawAccounts) {
        String accoutsInfo = rawAccounts.trim();
        String[] accounts = accoutsInfo.split("%");
        if (accounts.length < 1 || accounts.length > 3) {
            throw new IllegalArgumentException("[ERROR] 사용자의 계좌 개수가 1~3개 제한을 위반했습니다.");
        }
        return Arrays.stream(accounts)
                .map(MemberValidator::validateAccount)
                .toList();
    }

    /**
     * 계좌번호 = 숫자 12 자리
     * - 12 자리가 아닌 숫자인 경우
     * - 숫자가 아닌 문자들이 섞여있는 경우
     *
     * 계좌 비밀번호 = 숫자 4 자리
     * - 4 자리가 아닌 숫자인 경우
     * - 숫자가 아닌 문자들이 섞여있는 경우
     */
    private static Account validateAccount(String accountInfo) {
        String[] strings = accountInfo.split("&");
        if (strings.length != 2 ){
            throw new IllegalArgumentException();
        }
        String accountNumber = strings[0];
        String password = strings[1];
        boolean isValidAccountNumber = accountNumber.matches("\\d{12}");
        boolean isValidPassword = password.matches("\\d{4}");
        if (isValidAccountNumber && isValidPassword){
            return new Account(accountNumber, password);
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 계좌번호 데이터가 형식에 맞지 않습니다.");
    }
}
