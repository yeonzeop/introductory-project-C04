package BanKU.domain;

import java.time.LocalDate;

import static BanKU.utils.DateValidator.validateDate;
import static BanKU.utils.TransactionValidator.*;

public class Reservation {
    private final String senderAccountNumber;
    private final LocalDate transferDate;
    private final String receiverAccountNumber;
    private final long amount;
    private final String memo;

    public Reservation(String senderAccountNumber, LocalDate date, String receiverAccountNumber, long amount, String memo) {
        this.senderAccountNumber = senderAccountNumber;
        this.transferDate = date;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.memo = memo;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public long getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public static Reservation from(String[] reservationInfos) throws IllegalArgumentException{
        if (4 > reservationInfos.length || reservationInfos.length > 5) {
            throw new IllegalArgumentException("[ERROR] 유효하지 않은 예약송금 데이터입니다. 해당 행을 무시합니다.");
        }
        String senderAccountNumber = validateAccountNumber(reservationInfos[0]);
        LocalDate transferDate = validateDate(reservationInfos[1]);
        String receiverAccountNumber = validateAccountNumber(reservationInfos[2]);
        long amount = validateAmount(reservationInfos[3]);
        String memo = (reservationInfos.length == 4) ? null : validateMemo(reservationInfos[4]);
        return new Reservation(senderAccountNumber, transferDate, receiverAccountNumber, amount, memo);
    }

}
