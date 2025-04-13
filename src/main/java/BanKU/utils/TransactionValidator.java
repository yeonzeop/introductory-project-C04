package BanKU.utils;

public class TransactionValidator {

    private final static long AMOUNT_LIMIT = 10_000_000L;

    public static String validateAccountNumber(String rawAccountNumber) {
        String accountNumber = rawAccountNumber.trim();
        if (accountNumber.matches("\\d{12}")) {
            return accountNumber;
        }
        throw new IllegalArgumentException("[ERROR] 사용자의 계좌번호가 형식에 맞지 않습니다.");
    }

    /**
     * 거래금액 = 숫자
     * - 숫자가 아닌 문자들이 섞여있는 경우 무시
     * - 거래 한도 금액을 벗어난 경우 해당 행 무시
     */
    public static long validateAmount(String rawAmount) {
        if (!rawAmount.matches("\\d+")) {
            throw new IllegalArgumentException("[ERROR] 거래 금액에 숫자가 아닌 문자열이 포함되어 있습니다.");
        }
        long amount = Long.parseLong(rawAmount);
        if (amount > AMOUNT_LIMIT) {
            throw new IllegalArgumentException("[WARNING] 거래 금액 한도를 초과하여 누락된 거래 내역이 있습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("[ERROR] 거래 금액은 양수여야 합니다.");
        }
        return amount;
    }

    /**
     * 메모 : 글자 공백 포함 10 자리
     * - 구분자가 섞여있는 경우
     * - 10 글자를 초과하는 경우 앞의 10 글자만 유효하고 이후 글자는 무시
     */
    public static String validateMemo(String rawMemo) {
        if (rawMemo.contains("|") || rawMemo.contains("&") || rawMemo.contains("%")) {
            throw new IllegalArgumentException("[ERROR] 메모에 구분자가 포함되어 있습니다.");
        }
        return rawMemo.length() <= 10 ? rawMemo : rawMemo.substring(0, 10);
    }
}
