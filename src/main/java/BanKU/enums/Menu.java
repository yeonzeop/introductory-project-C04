package BanKU.enums;

import java.util.Scanner;

public enum Menu {

    DEPOSIT("입금", 1),
    WITHDRAWAL("출금", 2),
    TRANSFER("송금", 3),
    ACCOUNT_INQUIRY("계좌 조회", 4),
    ACCOUNT_CREATION("계좌 생성", 5),
    SAVING_ACCOUNT_CREATION("적금 계좌 생성", 6),
    SAVING_ACCOUNT_CLOSED("적금 해지", 7),
    TRANSFER_RESERVATION("예약 송금", 8),
    QUIT("종료", 9);

    private final String description;
    private final int menuNumber;

    Menu(String description, int menuNumber) {
        this.description = description;
        this.menuNumber = menuNumber;
    }

    public static Menu of(Scanner scanner) {
        while (true) {
            System.out.print("       메뉴를 입력해주세요(1~9 사이의 숫자) > ");
            String rawMenu = scanner.nextLine().trim();
            if (!rawMenu.matches("\\d{1}")) {
                System.out.println("[ERROR] 올바른 번호를 입력해주세요.");
                continue;
            }

            int intMenu = Integer.parseInt(rawMenu);
            for (Menu menu : values()) {
                if (intMenu == menu.getMenuNumber()) {
                    return menu;
                }
            }
        }
    }

    private int getMenuNumber() {
        return menuNumber;
    }
}
