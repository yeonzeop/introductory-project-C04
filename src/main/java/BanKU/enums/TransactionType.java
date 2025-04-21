package BanKU.enums;

public enum TransactionType {
    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    TRANSFER("송금");

    private final String type;

    TransactionType(String type) {
        this.type = type;
    }

    public static TransactionType from(String type) {
        for (TransactionType transactionType : values()) {
            if (transactionType.type.equals(type)) {
                return transactionType;
            }
        }
        throw new IllegalArgumentException("[ERROR] 잘못된 거래 유형입니다. type = " + type);
    }

    @Override
    public String toString(){
        return type;
    }
}
