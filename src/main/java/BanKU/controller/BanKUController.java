package BanKU.controller;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.enums.Menu;
import BanKU.repository.DateRepository;
import BanKU.repository.MemberRepository;
import BanKU.repository.ReservationRepository;
import BanKU.repository.TransactionRepository;
import BanKU.service.AccountService;
import BanKU.service.DateService;
import BanKU.service.MemberService;
import BanKU.view.OutputView;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;
import java.util.Scanner;

public class BanKUController {

    private final DateService dateService;
    private final AccountService accountService;
    private final MemberService memberService;

    public BanKUController(DateRepository dateRepository, MemberRepository memberRepository, TransactionRepository transactionRepository,ReservationRepository reservationRepository) {
        this.dateService = new DateService(dateRepository);
        this.accountService = new AccountService(memberRepository, transactionRepository,reservationRepository);
        this.memberService = new MemberService(memberRepository, transactionRepository);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        // 1. 날짜 입력받기
        System.out.println("====================================== BanKU ======================================");
        System.out.println("    Welcome! Start a better financial life with BanKU.");
        System.out.println("----------------------------------------------------------------------------------\n");
        LocalDate nowDate = dateService.getNowDate(scanner);
        accountService.setNow(nowDate);


        // 2. 로그인/회원가입 기능
        Member member = memberService.handleLoginOrSignup(scanner);

        if (member.getAccounts().isEmpty()) {
            System.out.println("BanKU: 현재 사용자 명의로 된 계좌가 개설되어 있지 않습니다.\n" +
                    "새로운 계좌 개설을 위해 <계좌 개설 메뉴>로 이동하겠습니다.");
            memberService.createAccount(nowDate, member, scanner);
        }

        // 3. 계좌 선택 (신규 회원인 경우 계좌 생성으로 바로 넘어가기)
        System.out.println("BanKU: 현재 사용자 명의로 된 계좌는 " + member.getAccounts().size() + "개 있습니다.");
        Account account = accountService.choose(member, scanner);

        // 4. 메인메뉴로 나옴
        while (true) {
            OutputView.showMenu();
            Menu menu = Menu.of(scanner);
            switch (Objects.requireNonNull(menu)) {
                case DEPOSIT -> accountService.deposit(account, scanner);
                case WITHDRAWAL -> accountService.withdrawal(account, scanner);
                case TRANSFER -> accountService.transfer(account, scanner);
                case ACCOUNT_INQUIRY -> {
                    System.out.println("BanKU: -----------------------------------------------------------------------------------\n" +
                            "                                          계 좌     조 회                                   \n" +
                            "       -----------------------------------------------------------------------------------");
                    OutputView.showAccounts(member.getAccounts());
                    System.out.print("BanKU: 메뉴로 돌아가시려면 'y' 혹은 'Y'키를 입력해주세요> ");
                    while (true) {
//                        System.out.print("BanKU: 메뉴로 돌아가시려면 'y' 혹은 'Y'키를 입력해주세요> ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                            break;
                        } else {
                            System.out.print("[ERROR] 잘못된 입력입니다. 문자 'y' 혹은 'Y'를 입력해주세요> ");
                        }
                    }
                }
                case ACCOUNT_CREATION -> memberService.createAccount(nowDate, member, scanner);
                case SAVING_ACCOUNT_CREATION -> memberService.createDepositAccount(nowDate, member, scanner);
                case SAVING_ACCOUNT_CLOSED -> memberService.closeDepositAccount(nowDate, member, scanner);
                case QUIT -> {
                    if (quit(scanner)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean quit(Scanner scanner) {
        System.out.print("BanKU: 정말 종료하시겠습니까? (y/n) > ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("y")) {
            System.out.println("BanKU: -----------------------------------------------------------------------------");
            System.out.println("                               BanKU를 이용해주셔서 감사합니다.                            ");
            System.out.println("       -----------------------------------------------------------------------------");
            return true;
        }
        if (!input.equalsIgnoreCase("n")) {
            System.out.println("[ERROR] 올바른 응답문자를 입력해주세요. 메뉴 화면으로 돌아갑니다.");
        }
        return false;
    }
}


