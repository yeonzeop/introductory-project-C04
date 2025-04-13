package BanKU.view;

import BanKU.domain.Account;
import BanKU.enums.Menu;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Scanner;

import static BanKU.utils.DateValidator.validateDate;
import static BanKU.utils.MemberValidator.*;


public class InputView {

    public static Menu getMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            try {
                return Menu.of(input);
            } catch (IllegalArgumentException e) {
                // 사용자가 메뉴 번호를 잘못 입력했을 경우 메시지 출력
                System.out.println("[ERROR] 메뉴는 1이상 6이하의 숫자로만 입력 가능합니다.");
            }
        }
    }

    public static Account chooseAccount() {
        return null;
    }

    public static MonthDay requestNowDate(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 날짜를 입력해주세요\n" +
                    "(MMDD 형식, 예: 0912) > ");
            try {
                return validateDate(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static boolean isExistingMember(Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 현재 회원이신가요? (y/n) > ");
            String answer = scanner.nextLine().trim();
            if (answer.equals("y")) {
                return true;
            }
            if (answer.equals("n")) {
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
}
