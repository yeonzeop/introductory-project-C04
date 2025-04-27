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
                "       6. 종료\n" +
                "       ------------------------------------------------------------------------\n" +
                "       메뉴를 입력해주세요(1~6 사이의 숫자) > ");
    }

    public static void showAccounts(List<Account> accounts) {
        // 계좌 목록 출력
        System.out.println("       +------------------------+--------------------------+");
        System.out.println("       |        계좌 번호         |           잔 액            |");
        System.out.println("       +------------------------+--------------------------+");

        for (Account account : accounts) {
            String accountNumber = centerAlign(account.getAccountNumber(), 22);
            String balanceStr = String.format("%,d원", account.getBalance());
            balanceStr = centerAlign(balanceStr, 24);

            System.out.println("       | " + accountNumber + " | " + balanceStr + " |");
            System.out.println("       +------------------------+--------------------------+");
        }

        while (true) {

        }
    }

    private static String centerAlign(String text, int width) {
        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;

        return " ".repeat(left) + text + " ".repeat(right);
    }

}
