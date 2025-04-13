package BanKU.domain;

public class Account {
    private final String accountNumber;       // 계좌번호
    private final String password;
    private long balance;                    // 잔액
    private boolean isActive;

    public Account(String accountNumber, String password) {
        this.accountNumber = accountNumber;
        this.password = password;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void plus(long amount) {
        // 오버플로우 검사
        if (balance > Long.MAX_VALUE - amount) {
            throw new IllegalArgumentException("[WARNING] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
        }
        balance += amount;
    }

    public void minus(long amount) {
        // 음수 검사
        if (balance - amount < 0) {
            throw new IllegalArgumentException("[WARNING] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
        }
        balance -= amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                ", isActive=" + isActive +
                '}';
    }

    public long getBalance() {
        return balance;
    }
}
