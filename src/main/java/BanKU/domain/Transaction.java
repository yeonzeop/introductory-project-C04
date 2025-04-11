package BanKU.domain;


import BanKU.enums.TransactionType;

import java.time.MonthDay;

import static BanKU.enums.TransactionType.DEPOSIT;
import static BanKU.utils.DateValidator.validateDate;
import static BanKU.utils.TransactionValidator.*;

public class Transaction {
    private final String senderAccountNumber;
    private final MonthDay date;                      // 거래일시
    private final TransactionType type;
    private final String receiverAccountNumber;
    private final long amount;
    private final String memo;

    public Transaction(String senderAccountNumber, MonthDay date, TransactionType type, String receiverAccountNumber, long amount, String memo) {
        this.senderAccountNumber = senderAccountNumber;
        this.date = date;
        this.type = type;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.memo = memo;       // Nullable
    }

    public static Transaction from(String[] transactionInfos) throws IllegalArgumentException {
        // 메모가 있으면 6개, 없으면 5개의 String이 존재함
        if (5 > transactionInfos.length || transactionInfos.length > 6) {
            throw new IllegalArgumentException("[ERROR] 유효하지 않은 거래내역 데이터입니다. 해당 행을 무시합니다.");
        }
        String senderAccountNumber = validateAccountNumber(transactionInfos[0]);
        MonthDay date = validateDate(transactionInfos[1]);
        TransactionType type = TransactionType.from(transactionInfos[2]);
        String receiverAccountNumber = validateAccountNumber(transactionInfos[3]);
        long amount = validateAmount(transactionInfos[4]);
        String memo = (transactionInfos.length == 5) ? null : validateMemo(transactionInfos[5]);
        return new Transaction(senderAccountNumber, date, type, receiverAccountNumber, amount, memo);
    }

    public void applyToAccounts(Account account) {
        if (type == DEPOSIT) {
            account.plus(amount);
        } else {
            account.minus(amount);
        }
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public MonthDay getDate() {
        return date;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "senderAccountNumber='" + senderAccountNumber + '\'' +
                ", date=" + date +
                ", type=" + type +
                ", receiverAccountNumber='" + receiverAccountNumber + '\'' +
                ", amount=" + amount +
                ", memo='" + memo + '\'' +
                '}';
    }
}
