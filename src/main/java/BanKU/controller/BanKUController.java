package BanKU.controller;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.enums.Menu;
import BanKU.repository.DateRepository;
import BanKU.repository.MemberRepository;
import BanKU.repository.TransactionRepository;
import BanKU.service.AccountService;
import BanKU.service.DateService;
import BanKU.service.MemberService;
import BanKU.view.OutputView;

import java.time.MonthDay;
import java.util.Scanner;

public class BanKUController {

    private final DateService dateService;
    private final AccountService accountService;
    private final MemberService memberService;

    public BanKUController(DateRepository dateRepository, MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.dateService = new DateService(dateRepository);
        this.accountService = new AccountService(memberRepository, transactionRepository);
        this.memberService = new MemberService(memberRepository);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        // 1. 날짜 입력받기
        MonthDay nowDate = dateService.getNowDate(scanner);
        accountService.setNow(nowDate);

        // 2. 로그인/회원가입 기능
        Member member = memberService.handleLoginOrSignup(scanner);

        if (member.getAccounts().isEmpty()) {
            System.out.println("BanKU: 현재 사용자 명의로 된 계좌가 개설되어 있지 않습니다.\n" +
                    "새로운 계좌 개설을 위해 <계좌 개설 메뉴>로 이동하겠습니다.");
            memberService.createAccount(member, scanner);
        }

        // 3. 계좌 선택 (신규 회원인 경우 계좌 생성으로 바로 넘어가기)
        System.out.println("BanKU: 현재 사용자 명의로 된 계좌는 " + member.getAccounts().size() + "개 있습니다.");
        Account account = accountService.choose(member, scanner);

        // 4. 메인메뉴로 나옴
        while (true) {
            OutputView.showMenu();
            Menu menu = Menu.of(scanner.nextLine());
            if (menu == null) {
                continue;
            }
            switch (menu) {
                case DEPOSIT -> accountService.deposit(account, scanner);
                case WITHDRAWAL -> accountService.withdrawal(account, scanner);
                case TRANSFER -> accountService.transfer(account, scanner);
                case ACCOUNT_INQUIRY -> {
                    System.out.println("BanKU: -----------------------------------------------------------------------------------\n" +
                                       "                                          계 좌     조 회                                   \n" +
                                       "       -----------------------------------------------------------------------------------");
                    OutputView.showAccounts(member.getAccounts());
                }
                case ACCOUNT_CREATION -> memberService.createAccount(member, scanner);
                case QUIT -> {
                    return;
                }
            }
        }
    }
}
