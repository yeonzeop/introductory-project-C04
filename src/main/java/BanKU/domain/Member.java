package BanKU.domain;

import java.time.LocalDate;
import java.util.List;

import static BanKU.utils.MemberValidator.*;

public class Member {

    private final String loginId;
    private final String loginPassword;
    private final String name;
    private final LocalDate birthday;
    private final String phoneNumber;
    private final List<Account> accounts;

    public Member(String loginId, String loginPassword, String name, LocalDate birthday, String phoneNumber, List<Account> accounts) {
        this.loginId = loginId;
        this.loginPassword = loginPassword;
        this.name = name;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.accounts = accounts;
    }


    public static Member from(String[] memberInfos) {
        if (memberInfos.length != 6) {
            System.out.println("[ERROR] 유효하지 않은 회원 정보가 데이터입니다. 해당 행을 무시합니다.");
            return null;
        }
        try {
            String loginId = validateLoginId(memberInfos[0]);
            String loginPassword = validatePassword(memberInfos[1]);
            String name = validateName(memberInfos[2]);
            LocalDate birthday = validateBirthday(memberInfos[3]);
            String phoneNumber = validatePhoneNumber(memberInfos[4]);
            List<Account> accounts = createAccounts(memberInfos[5]);
            return new Member(loginId, loginPassword, name, birthday, phoneNumber, accounts);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getLoginId() {
        return loginId;
    }

    public String getName() {
        return name;
    }

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
                ", loginPassword='" + loginPassword + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accounts=" + accounts +
                '}';
    }
// 아이디 입력
    // 로그인 비밀번호 입력
    // 이름 입력
    // 생년월일 입력

}
