package BanKU.utils;

public class TransactionValidator {

    public final static long AMOUNT_LIMIT = Long.MAX_VALUE;

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
            throw new IllegalArgumentException("[ERROR] 금액은 숫자로만 입력 가능합니다. 원 단위에 맞춰 금액을 다시 입력해주세요.");
        }
        try {
            long amount = Long.parseLong(rawAmount);
            if (amount > AMOUNT_LIMIT) {
                throw new IllegalArgumentException("[WARNING] 거래 금액 한도를 초과하여 누락된 거래 내역이 있습니다.");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("[ERROR] 거래 금액은 양수여야 합니다.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("[WARNING] 거래 금액 한도를 초과하여 누락된 거래 내역이 있습니다.");
        }
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
