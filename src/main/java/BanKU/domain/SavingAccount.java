package BanKU.domain;

import BanKU.enums.SavingsType;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static BanKU.enums.TransactionType.DEPOSIT;
import static BanKU.utils.DateValidator.validateDate;
import static BanKU.utils.MemberValidator.validateAccount;
import static BanKU.utils.MemberValidator.validateLoginId;

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
            default -> {
            }
        }
    }

    public double getRate(LocalDate nowDate) {
        return !nowDate.isBefore(endDay) ? rate : earlyRate;
    }

    public double getEarlyRate() {
        return earlyRate;
    }

    public long computeInterest(LocalDate nowDate, List<Transaction> transactions) {
        double totalInterest = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getType() != DEPOSIT) continue; // 입금만 계산 대상

            long operatingDays = ChronoUnit.DAYS.between(transaction.getDate(), (isClosed|| nowDate.isAfter(endDay)) ? endDay : nowDate);
            if (operatingDays < 0) continue; // 잘못된 날짜는 무시

            double rateToApply = getRate(nowDate); // 조기해지 여부에 따라 적용 이율 결정

            // 윤년인지에 따라 일수 기준 결정
            int baseDays = Year.isLeap(nowDate.getYear()) ? 366 : 365;
            double interest = transaction.getAmount() * rateToApply * ((double) operatingDays / baseDays);
            totalInterest += interest;
        }

        return (long) Math.ceil(totalInterest); // 소수점 이하 올림
    }

    public long computeTotalDeposited(List<Transaction> transactions) {
        long totalDeposited = 0;
        for (Transaction transaction : transactions) {
//            System.out.println("[LOG] 거래내역 = "+transaction.toString());
            totalDeposited += transaction.getAmount();
        }
        return totalDeposited;
    }

    public void setClosed() {
        isClosed = true;
    }

    public static SavingAccount from(String[] strings) {
        if (strings.length != 6) {
            System.out.println("[ERROR] 유효하지 않은 회원 정보가 데이터입니다. 해당 행을 무시합니다.");
            return null;
        }
            validateLoginId(strings[0]);
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

    }

    private static SavingsType determineSavingsType(LocalDate startDay, LocalDate endDay) {
        long monthsBetween = ChronoUnit.MONTHS.between(startDay.withDayOfMonth(1), endDay.withDayOfMonth(1));

        return switch ((int) monthsBetween) {
            case 6 -> SavingsType.SHORT;
            case 12 -> SavingsType.MID;
            case 18 -> SavingsType.LONG;
            default -> {
                System.out.println("[ERROR] 적금 기간은 6개월, 12개월, 18개월 중 하나여야 합니다.");
                System.exit(0);
                throw new IllegalStateException("[ERROR] 적금 기간은 6개월, 12개월, 18개월 중 하나여야 합니다.");
            }
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
