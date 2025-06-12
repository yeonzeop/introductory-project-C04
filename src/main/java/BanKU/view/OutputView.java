package BanKU.view;

import BanKU.domain.Account;

import java.util.List;

public class OutputView {

    public static void showMenu() {
        System.out.print("       -----------------------------------------------------------------------\n" +
                "                                        메 뉴                                  \n" +
                "       -----------------------------------------------------------------------\n" +
                "       1. 입금\n" +
                "       2. 출금\n" +
                "       3. 송금\n" +
                "       4. 계좌 조회\n" +
                "       5. 계좌 생성\n" +
                "       6. 적금 계좌 가입\n" +
                "       7. 적금 계좌 해지\n" +
                "       8. 예약 송금\n" +
                "       9. 종료\n" +
                "       ------------------------------------------------------------------------\n");
    }

    public static void showAccounts(List<Account> accounts) {
        // 계좌 목록 출력
        System.out.println("       +------------------------+--------------------------+");
        System.out.println("       |        계좌 번호         |           잔 액            |");
        System.out.println("       +------------------------+--------------------------+");


        for (Account account : accounts) {
            if (!account.isActive()) continue;      // 비활성 계좌는 건너뜀

            String accountNumber = centerAlign(account.getAccountNumber(), 22);
            String balanceStr = String.format("%,d원", account.getBalance());
            balanceStr = centerAlign(balanceStr, 24);

            System.out.println("       | " + accountNumber + " | " + balanceStr + " |");
            System.out.println("       +------------------------+--------------------------+");
        }
    }

    private static String centerAlign(String text, int width) {
        if (text.length() >= width) {
            return text; // 너무 길면 그냥 그대로 반환
        }

        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;

        return " ".repeat(left) + text + " ".repeat(right);
    }

}
