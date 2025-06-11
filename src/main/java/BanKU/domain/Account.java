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
            throw new IllegalArgumentException("[ERROR] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
        }
//        System.out.println("[plus LOG] "+accountNumber + " 계좌번호에 " + amount + "원 추가");
        balance += amount;
    }

    public boolean canAcceptAmount(long amount) {
        return balance <= Long.MAX_VALUE - amount;
    }

    public void minus(long amount) {
        // 음수 검사
        if (balance - amount < 0) {
            throw new IllegalArgumentException("[ERROR] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
        }
        if(this instanceof SavingAccount && !((SavingAccount) this).isClosed()){
            throw new IllegalArgumentException("[ERROR] 해지하지 않은 적금 계좌는 출금 거래가 불가능 합니다.");
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

    public String getPassword() {
        return password;
    }
}
