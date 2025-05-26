package BanKU.view;

import BanKU.domain.Account;
import BanKU.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static BanKU.enums.TransactionType.DEPOSIT;
import static BanKU.enums.TransactionType.WITHDRAWAL;
import static BanKU.utils.MemberValidator.*;
import static BanKU.utils.TransactionValidator.validateAccountNumber;


public class InputView {

    public static Account chooseAccount(List<Account> accounts, Scanner scanner) {
        while (true) {
            try {
                System.out.print("BanKU: 거래를 원하는 계좌번호를 입력해주세요(-없이 숫자로만 입력해주세요) > ");
                String accountNumber = validateAccountNumber(scanner.nextLine().trim());
                for (Account account : accounts) {
                    if (account.getAccountNumber().equals(accountNumber)) {
                        return account;
                    }
                }
                throw new IllegalArgumentException("[ERROR] 존재하지 않는 계좌번호입니다.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String requestNowDate(Scanner scanner) {
        System.out.print("BanKU: 날짜를 입력해주세요\n" +
                "(MMDD 형식, 예: 250912) > ");
        return scanner.nextLine();
    }

    public static boolean isExistingMember(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 현재 회원이신가요? (y/n) > ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("[ERROR] 영문자 y나 n 중 하나를 입력해주세요");
        }
    }


    public static String requestId(Scanner scanner, String requestText) {
        while (true) {
            System.out.print(requestText);
            try {
                return validateLoginId(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String requestPassword(Scanner scanner, String requestText) {
        while (true) {
            System.out.print(requestText);
            try {
                return validatePassword(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String requestName(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 이름을 입력해주세요.\n" +
                    "(최대 4자까지의 한글 이름만 사용할 수 있습니다.) > ");
            try {
                return validateName(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static LocalDate requestBirthday(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 생년월일을 입력해주세요.\n" +
                    "(YYMMDD 형식, 예: 041023) > ");
            try {
                return validateBirthday(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String requestPhoneNumber(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 전화번호에서 '010'을 제외한 뒷자리 8자리를 입력해주세요\n" +
                    "(예: 12345678) > ");
            try {
                return validatePhoneNumber(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String requestReceiverAccount(Account account, Scanner scanner, boolean isFirst) {
        if (isFirst) {
            System.out.println("BanKU: ----------------------------------------------------------------------------\n" +
                    "                                         송     금                                  \n" +
                    "       ----------------------------------------------------------------------------\n" +
                    "       송금할 계좌(사용자 계좌): " + account.getAccountNumber() + " 잔액(단위: 원): " + account.getBalance() + "원\n");
        }
        System.out.print("       송금받을 계좌번호를 입력해주세요(-없이 숫자로만 입력해주세요) > ");
        return scanner.nextLine().trim();
    }

    public static String requestAmount(Account account, Scanner scanner, TransactionType type, boolean isFirst) {
        if (type == DEPOSIT) {
            if (isFirst) {
                System.out.println("BanKU: ----------------------------------------------------------------------------\n" +
                        "                                         입     금                                  \n" +
                        "       ----------------------------------------------------------------------------\n" +
                        "       입금할 계좌(사용자 계좌): " + account.getAccountNumber() + " 잔액(단위: 원): " + account.getBalance() + "원\n");
            }
            System.out.print("       입금할 금액을 입력해주세요(단위: 원) > ");
        } else if (type == WITHDRAWAL) {
            if (isFirst) {
                System.out.println("BanKU: ----------------------------------------------------------------------------\n" +
                        "                                        출     금                                  \n" +
                        "       ----------------------------------------------------------------------------\n" +
                        "       출금할 계좌(사용자 계좌): " + account.getAccountNumber() + " 잔액(단위: 원): " + account.getBalance() + "원\n");
            }
            System.out.print("       출금할 금액을 입력해주세요(단위: 원) > ");
        } else {
            System.out.print("BanKU: 송금할 금액을 입력해주세요(단위: 원) > ");
        }
        return scanner.nextLine();
    }
}
