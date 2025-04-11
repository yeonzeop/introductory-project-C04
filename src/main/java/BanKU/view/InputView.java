package BanKU.view;

import BanKU.domain.Account;
import BanKU.enums.Menu;

import java.time.MonthDay;
import java.util.List;
import java.util.Scanner;

import static BanKU.utils.DateValidator.validateDate;


public class InputView {
    public static Menu getMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true){
            String input = scanner.next();
            try {
                return Menu.of(input);
            } catch (IllegalArgumentException e){
                System.out.println("다시입력하십쇼");
                // 사용자가 메뉴 번호를 잘못 입력했을 경우 메시지 출력
                continue;
            }
        }
    }

    public static void showAccounts(List<Account> accounts) {

    }

    public static Account chooseAccount() {
        return null;
    }

    public static MonthDay requestNowDate(Scanner scanner) {
        /**
         * TODO. 현재 날짜 입력 요청 프롬프트 출력
         * BanKU: 날짜를 입력해주세요
         * (MMDD 형식, 예: 0912) > 202020 Enter
         * [ERROR] 4자리 숫자를 입력해주세요.
         * 예외 발생 시 IllegalArgumentException("~~오류메시지~~");
         * 이렇게 처리해주시면 됩니다.
         */
        return validateDate(scanner.next());
    }

    public static boolean isExistingMember(Scanner scanner) {
        /** TODO.
         * BanKU: 현재 회원이신가요? (y/n) > M
         * [ERROR] 영문자 y나 n 중 하나를 입력해주세요
         * 예외 발생 시 IllegalArgumentException("~~오류메시지~~");
         * 이렇게 처리해주시면 됩니다.
         */
        return true;
    }
}
