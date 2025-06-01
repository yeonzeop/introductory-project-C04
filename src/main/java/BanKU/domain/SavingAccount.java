package BanKU.domain;

import BanKU.enums.SavingsType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static BanKU.utils.DateValidator.validateDate;
import static BanKU.utils.MemberValidator.*;

public class SavingAccount extends Account {

    private LocalDate startDay;
    private LocalDate endDay;
    private double rate;            // 만기 해지 시 이율
    private double earlyRate;       // 중도 해지 시 이율
    private boolean isClosed;       // 적금 해지 여부

    public SavingAccount(String accountNumber, String password, LocalDate startDay, SavingsType type, boolean isClosed) {
        super(accountNumber, password);
        this.startDay = startDay;
        this.isClosed = isClosed;

        switch (type) {
            case SHORT -> {
                this.endDay = startDay.plusMonths(6);
                this.rate = 0.02;
                this.earlyRate = 0.001;
            }
            case MID -> {
                this.endDay = startDay.plusMonths(12);
                this.rate = 0.03;
                this.earlyRate = 0.005;
            }
            case LONG -> {
                this.endDay = startDay.plusMonths(18);
                this.rate = 0.04;
                this.earlyRate = 0.01;
            }
            default -> {}
        }
    }

    public double getRate() {
        return rate;
    }

    public double getEarlyRate() {
        return earlyRate;
    }

    public long computeInterest(List<Transaction> transactions) {
        double totalInterest = 0.0;
        boolean isLeapYear = endDay.isLeapYear();       // 윤년인지 아닌지 판단
        double daysInYear = isLeapYear ? 366.0 : 365.0;

        for (Transaction transaction : transactions) {
            long days = ChronoUnit.DAYS.between(transaction.getDate(), endDay);
            double applicableRate = isClosed ? getRate() : getEarlyRate();
            double interest = transaction.getAmount() * applicableRate * (days / daysInYear);
            totalInterest += interest;
        }

        return (long) Math.floor(totalInterest);
    }

    public void setClosed() {
        isClosed = true;
    }

    public static SavingAccount from(String[] strings) {
        if (strings.length != 6) {
            System.out.println("[ERROR] 유효하지 않은 회원 정보가 데이터입니다. 해당 행을 무시합니다.");
            return null;
        }
        try {
            String loginId = validateLoginId(strings[0]);
            Account account = validateAccount(strings[1] + "&" + strings[2]);
            LocalDate startDay = validateDate(strings[3]);
            LocalDate endDay = validateDate(strings[4]);

            SavingsType type = determineSavingsType(startDay, endDay);

            boolean isClosed = !strings[5].equalsIgnoreCase("opened");

            return new SavingAccount(account.getAccountNumber(),
                    account.getPassword(),
                    startDay,
                    type,
                    isClosed);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static SavingsType determineSavingsType(LocalDate startDay, LocalDate endDay) {
        long monthsBetween = ChronoUnit.MONTHS.between(startDay.withDayOfMonth(1), endDay.withDayOfMonth(1));

        return switch ((int) monthsBetween) {
            case 6 -> SavingsType.SHORT;
            case 12 -> SavingsType.MID;
            case 18 -> SavingsType.LONG;
            default -> throw new IllegalArgumentException("[ERROR] 적금 기간은 6개월, 12개월, 18개월 중 하나여야 합니다.");
        };
    }
    public LocalDate getStartDay() {
        return startDay;
    }

    public LocalDate getEndDay() {
        return endDay;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
