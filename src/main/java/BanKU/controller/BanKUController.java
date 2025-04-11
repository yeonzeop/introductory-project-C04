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
import BanKU.view.InputView;

import java.time.MonthDay;

public class BanKUController {

    private final DateService dateService;
    private final AccountService accountService;
    private final MemberService memberService;

    public BanKUController(DateRepository dateRepository, MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.dateService = new DateService(dateRepository);
        this.accountService = new AccountService(transactionRepository);
        this.memberService = new MemberService(memberRepository);
    }

    public void run() {
        // 1. 날짜 입력받기
        MonthDay nowDate = dateService.getNowDate();
        accountService.setNow(nowDate);

        // 2. 로그인/회원가입 기능
        Member member = memberService.handleLoginOrSignup();

        // 3. 계좌 선택 (신규 회원인 경우 계좌 생성으로 바로 넘어가기)
        Account account = accountService.choose(member);

        // 4. 메인메뉴로 나옴
        while (true) {
            Menu menu = InputView.getMenu();
            switch (menu) {
                case DEPOSIT -> accountService.deposit(account);
                case WITHDRAWAL -> accountService.withdrawal(account);
                case TRANSFER -> accountService.transfer(account);
                case ACCOUNT_INQUIRY -> InputView.showAccounts(member.getAccounts());
                case ACCOUNT_CREATION -> memberService.createAccount(member);
                case QUIT -> {
                    return;
                }
            }
        }
    }
}
