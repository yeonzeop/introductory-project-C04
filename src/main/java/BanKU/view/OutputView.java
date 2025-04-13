package BanKU.view;

import BanKU.domain.Account;

import java.util.List;

public class OutputView {
    public static void showAccounts(String name, List<Account> accounts) {
        // 계좌 목록 출력
        System.out.println("[" + name + "]님의 사용가능한 계좌 목록");
        System.out.println("+------------------------+--------------------------+");
        System.out.println("|        계좌 번호         |           잔 액           |");
        System.out.println("+------------------------+--------------------------+");

        for (Account account : accounts) {
            String accountNumber = centerAlign(account.getAccountNumber(), 22);
            String balanceStr = String.format("%,d원", account.getBalance());
            balanceStr = centerAlign(balanceStr, 24);

            System.out.printf("| %s | %s |\n", accountNumber, balanceStr);
            System.out.println("+------------------------+--------------------------+");
        }
    }

    private static String centerAlign(String text, int width) {
        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;

        return " ".repeat(left) + text + " ".repeat(right);
    }

}
