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

    public static Menu of(String input) throws IllegalArgumentException {
        // 검증
        // 잘못 만들었으면 예외 던지기
        return DEPOSIT;
    }
}
