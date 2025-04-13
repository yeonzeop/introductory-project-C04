package BanKU.service;

import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.repository.MemberRepository;
import BanKU.view.InputView;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Scanner;

public class MemberService {

    private final static String SIGNUP_ID = "BanKU: 설정할 아이디를 입력해주세요\n" +
            "(공백을 제외하고, 숫자와 영문자를 합쳐 최대 10자까지만 설정가능합니다.) > ";
    private final static String LOGIN_ID = "BanKU: 아이디를 입력해주세요 > ";
    private final static String SIGNUP_PASSWORD = "BanKU: 설정할 비밀번호를 입력해주세요 (숫자 6자리) > ";
    private final static String LOGIN_PASSWORD = "BanKU: 비밀번호를 입력해주세요 > ";

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member handleLoginOrSignup(Scanner scanner) {
        if (isMember(scanner)) {
            return login(scanner);
        }
        return signup(scanner);
    }

    private boolean isMember(Scanner scanner) {
        while (true) {
            try {
                return InputView.isExistingMember(scanner);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public Member login(Scanner scanner) {
        Member member;
        while (true) {
            try {
                String loginId = InputView.requestId(scanner, LOGIN_ID);
                member = memberRepository.findMemberByLoginId(loginId);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            String password = InputView.requestPassword(scanner, LOGIN_PASSWORD);
            if (!member.getPassword().equals(password)) {
                System.out.println("[ERROR] 비밀번호를 다시 확인해주세요");
                continue;
            }
            return member;
        }
    }


    private Member signup(Scanner scanner) {
        String loginId = requestUniqueLoginId(scanner);
        String password = InputView.requestPassword(scanner, SIGNUP_PASSWORD);
        String name = InputView.requestName(scanner);
        LocalDate birthday = InputView.requestBirthday(scanner);
        String phoneNumber = InputView.requestPhoneNumber(scanner);     // TODO. 전화번호 입력받는 프롬프트가 없는데, 데이터 파일에는 있어서 추가해놨습니다!

        Member member = new Member(loginId, password, name, birthday, phoneNumber);
        memberRepository.saveMember(member);
        return member;
    }

    private String requestUniqueLoginId(Scanner scanner) {
        while (true) {
            String loginId = InputView.requestId(scanner, SIGNUP_ID);
            if (memberRepository.isExistingLoginId(loginId)) {
                System.out.println("[ERROR] 이미 존재하는 아이디입니다. 다른 아이디를 입력해주세요.");
            }
            return loginId;
        }
    }

    public void createAccount(Member member) {
        if (member.getAccounts().size() >= 3) {
            System.out.println("[ERROR] 계좌가 3개 이상입니다. 추가 계좌를 생성할 수 없습니다.");
            return;
        }
        String accountNumber = generateUniqueAccountNumber();
        String password = InputView.requestAccountPassword();

        Account account = new Account(accountNumber, password);
        member.addAccount(account);
        memberRepository.saveAccount(member, account);
    }

    private String generateUniqueAccountNumber() {
        SecureRandom random = new SecureRandom();
        while (true) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 8) {
                sb.append(random.nextInt(10));      // 0~9 중 하나 추가
            }
            String accountNumber = "1123" + sb.toString();
            if (memberRepository.isPresentAccount(accountNumber)) {
                continue;
            }
            return accountNumber;
        }
    }
}
