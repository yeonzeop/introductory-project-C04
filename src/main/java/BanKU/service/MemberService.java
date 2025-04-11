package BanKU.service;

import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.repository.MemberRepository;
import BanKU.view.InputView;

import java.util.Scanner;

public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member handleLoginOrSignup() {
        if (isMember()) {
            return login();
        }
        return signup();
    }

    private boolean isMember() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                return InputView.isExistingMember(scanner);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Member login() {
        // 아이디 입력 -> Q. 일치하는 아이디가 없어도 다시 안돌아가나????
        // 로그인 비밀번호 입력 -> 얘만 반복
        return null;
    }

    private Member signup() {
        // 아이디 입력
        // 로그인 비밀번호 입력
        // 이름 입력
        // 생년월일 입력
//        Member member = new Member();

//        memberRepository.saveMember(member);
//        return member;
        return null;
    }


    public void createAccount(Member member) {
        Account account = new Account("", "");
        memberRepository.saveAccount(account);
    }
}
