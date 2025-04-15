package BanKU.enums;

public enum Menu {

    DEPOSIT("입금", 1),
    WITHDRAWAL("출금", 2),
    TRANSFER("송금", 3),
    ACCOUNT_INQUIRY("계좌 조회", 4),
    ACCOUNT_CREATION("계좌 생성", 5),
    QUIT("종료", 6);

    private final String description;
    private final int menuNumber;

    Menu(String description, int menuNumber) {
        this.description = description;
        this.menuNumber = menuNumber;
    }

    public static Menu of(String rawMenu) throws IllegalArgumentException {
        if (!rawMenu.matches("\\d{1}")) {
            System.out.println("[ERROR] 메뉴는 1이상 6이하의 숫자로만 입력 가능합니다.");
        }
        int intMenu = Integer.parseInt(rawMenu);
        for (Menu menu : values()) {
            if (intMenu == menu.getMenuNumber()) {
                return menu;
            }
        }
        System.out.println("[ERROR] 메뉴는 1이상 6이하의 숫자로만 입력 가능합니다.");
        return null;
    }

    private int getMenuNumber() {
        return menuNumber;
    }
}
