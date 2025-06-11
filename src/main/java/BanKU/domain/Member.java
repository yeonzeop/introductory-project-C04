package BanKU.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static BanKU.utils.MemberValidator.*;

public class Member {

    private final String loginId;
    private final String password;
    private final String name;
    private final LocalDate birthday;
    private String phoneNumber;
    private List<Account> accounts;
    private boolean hasSavingAccount = true;

    public Member(String loginId, String password, String name, LocalDate birthday, String phoneNumber, List<Account> accounts) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.accounts = accounts;
    }

    public Member(String loginId, String password, String name, LocalDate birthday, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }

    public static Member from(String[] memberInfos) {
        if (5 > memberInfos.length || memberInfos.length > 6) {
            System.out.println("[ERROR] 유효하지 않은 회원 정보가 데이터입니다. 해당 행을 무시합니다.");
            return null;
        }
        try {
            String loginId = validateLoginId(memberInfos[0]);
            String password = validatePassword(memberInfos[1]);
            String name = validateName(memberInfos[2]);
            LocalDate birthday = validateBirthday(memberInfos[3]);
            String phoneNumber = validatePhoneNumber(memberInfos[4]);
            List<Account> accounts = new ArrayList<>();
            if (memberInfos.length == 6) {
                accounts = createAccounts(memberInfos[5]);
            }
            return new Member(loginId, password, name, birthday, phoneNumber, accounts);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthday() { return birthday; } // 만들어도 되나요..?

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    @Override
    public String toString() {
        return "Member{" +
                "loginId='" + loginId + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accounts=" + accounts +
                '}';
    }

    public void addAccount(Account account) {
        long count = accounts.stream()
                .filter(Account::isActive)
                .count();
        if (count <= 3 || !account.isActive()) {
            accounts.add(account);
        }
    }

    public void removeAccount(String accountNumber) {
        accounts.removeIf(account -> account.getAccountNumber().equals(accountNumber));
    }

    public boolean hasSavingAccount() {
        return accounts.stream()
                .anyMatch(account -> (account instanceof SavingAccount) && account.isActive());
    }

//    public void setHasSavingAccount(boolean hasSavingAccount) {
//        this.hasSavingAccount = hasSavingAccount;
//    }

    public List<Account> getRegularAccounts() {
        return accounts.stream()
                .filter(account -> !(account instanceof SavingAccount) && account.isActive())
                .toList();
    }
}
