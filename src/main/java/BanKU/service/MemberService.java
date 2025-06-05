package BanKU.service;

import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.domain.SavingAccount;
import BanKU.domain.Transaction;
import BanKU.enums.SavingsType;
import BanKU.repository.MemberRepository;
import BanKU.repository.TransactionRepository;
import BanKU.view.InputView;
import BanKU.view.OutputView;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

import static BanKU.enums.TransactionType.DEPOSIT;

public class MemberService {

    private final static String SIGNUP_ID = "BanKU: 설정할 아이디를 입력해주세요\n" +
            "(공백을 제외하고, 숫자와 영문자를 합쳐 최대 10자까지만 설정가능합니다.) > ";
    private final static String LOGIN_ID = "BanKU: 아이디를 입력해주세요 > ";
    private final static String SIGNUP_PASSWORD = "BanKU: 설정할 비밀번호를 입력해주세요 (숫자 6자리) > ";
    private final static String LOGIN_PASSWORD = "BanKU: 비밀번호를 입력해주세요 > ";

    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;


    public MemberService(MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
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
                continue;
            }
            return loginId;
        }
    }

    public void createAccount(LocalDate nowDate, Member member, Scanner scanner) {
        System.out.println("BanKU: ---------------------------------------------------------------------------\n" +
                "                                     계 좌        생 성                             \n" +
                "       ----------------------------------------------------------------------------");
        OutputView.showAccounts(member.getAccounts());
        if (member.getAccounts().size() >= 3) {
            System.out.println("BanKU: 더이상 계좌를 생성할 수 없습니다.");
            System.out.print("BanKU: 메뉴로 돌아가시려면 'y' 혹은 'Y'키를 입력해 주세요 > ");
            while (true) {
                if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    break;
                }
                System.out.print("[ERROR] 잘못된 입력입니다. 문자 ‘y’ 혹은 ‘Y’를 입력해주세요> ");
            }
            return;
        }

        String accountNumber = generateUniqueAccountNumber(nowDate);
        String password = generatePassword(scanner);
        Account account = new Account(accountNumber, password);
        member.addAccount(account);
        memberRepository.saveAccount(member, account);
        System.out.println("BanKU: 계좌번호: " + accountNumber +
                "\nBanKU: 계좌 생성이 완료되었습니다.");
    }

    private String generateUniqueAccountNumber(LocalDate nowDate) {
        SecureRandom random = new SecureRandom();
        while (true) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 6) {
                sb.append(random.nextInt(10));      // 0~9 중 하나 추가
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd"); // 흠 이건 어카지...
            String accountNumber = nowDate.format(formatter) + sb.toString();
            if (memberRepository.isPresentAccount(accountNumber)) {
                continue;
            }
            return accountNumber;
        }
    }

    public void createDepositAccount(LocalDate nowDate, Member member, Scanner scanner) {
        if (member.hasSavingAccount()) {
            System.out.println("이미 적금 계좌가 존재합니다. 한 개의 적금 계좌만 만들 수 있습니다.");
            return;
        }

        SavingsType type = selectDepositProduct(scanner);
        String accountNumber = generateUniqueAccountNumber(nowDate);
        String password = generatePassword(scanner);
        SavingAccount savingAccount = new SavingAccount(accountNumber, password, nowDate, type, false);
        member.addAccount(savingAccount);
        member.setHasSavingAccount(true);
        memberRepository.saveSavingsAccount(member, savingAccount);
        System.out.println("BanKU: 적금 계좌번호: " + accountNumber + "\nBanKU: 적금 계좌 생성이 완료되었습니다.");
    }

    private String generatePassword(Scanner scanner) {
        String password;
        System.out.println("BanKU: 해당 계좌의 비밀번호(4자리 숫자)를 설정해주세요.\n" +
                "-----------------------------------------------------------------------------------");
        while (true) {
            System.out.print("비밀번호 > ");
            password = scanner.nextLine();
            if (password.matches("\\d{4}")) {
                break;
            }
            System.out.println("[ERROR] 올바르지 않은 입력입니다. 위 규칙에 알맞은 비밀번호를 입력해주세요");
        }
        return password;
    }

    public void closeDepositAccount(LocalDate nowDate, Member member, Scanner scanner) {
        if (!member.hasSavingAccount()) {
            System.out.println("BanKU: 적금 계좌가 존재하지 않습니다.");
            return;
        }
        SavingAccount savingAccount = member.getAccounts().stream()
                .filter(account -> account.isActive() && account instanceof SavingAccount)
                .map(account -> (SavingAccount) account)
                .findFirst()
                .orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // 가입 개월 수 계산
        long monthsBetween = ChronoUnit.MONTHS.between(savingAccount.getStartDay(), savingAccount.getEndDay());
        String productName;
        if (monthsBetween == 6) {
            productName = "자유 적금 - 6개월";
        } else if (monthsBetween == 12) {
            productName = "자유 적금 - 12개월";
        } else {
            productName = "자유 적금 - 18개월";
        }

        System.out.println("BanKU: --------------------------------------------------------------------------");
        System.out.println("                                   적금           해지                             ");
        System.out.println("       --------------------------------------------------------------------------");
        System.out.printf("       적금 가입일: %s (%d일)%15s가입 상품 명: %s%n",
                savingAccount.getStartDay().format(formatter),
                savingAccount.getStartDay().isLeapYear() ? 366 : 365,
                "",
                productName
        );
        System.out.printf("       적용 금리: %.1f%%%30s잔액(단위: 원):     %,d원%n",
                savingAccount.getRate() * 100,
                "",
                (int) savingAccount.getBalance()
        );
        System.out.println();
        if (nowDate.isBefore(savingAccount.getEndDay())) {
            System.out.print("       적금 만기일에 도달 전입니다. 정말 해지하시겠습니까? (y/n)> ");
        } else {
            System.out.print("       적금 만기일에 도달했습니다. 정말 해지하시겠습니까? (y/n)> ");
        }
        String input = scanner.nextLine().trim();
        if (!input.equalsIgnoreCase("y")) {
            System.out.println("적금 해지를 취소하였습니다.");
            return;
        }

        List<Transaction> transactions = transactionRepository.findSavingTransactionByAccount(savingAccount);

        long totalAmount = savingAccount.computeInterest(transactions);


        List<Account> regularAccounts = member.getAccounts().stream()
                .filter(account -> !(account instanceof SavingAccount) &&
                        account.isActive() &&
                        account.canAcceptAmount(totalAmount))
                .toList();

        // 기본 입출금 게좌들이 이를 모두 수용할 수 없다면  바로 적금 계좌를 동결
        if (regularAccounts.isEmpty()) {
            savingAccount.deactivate();
            savingAccount.setClosed();
            member.setHasSavingAccount(false);
            memberRepository.setSavingsAccountClosed(member, savingAccount);
            System.out.println("입금 가능한 계좌가 없어 적금 계좌가 동결되었습니다.");
            return;
        }

        Account receivingAccount = chooseReceivingAccount(scanner, regularAccounts);
        receivingAccount.plus(totalAmount);
        try {
            transactionRepository.save(new Transaction(
                    savingAccount.getAccountNumber(),
                    nowDate,
                    DEPOSIT,
                    receivingAccount.getAccountNumber(),
                    totalAmount,
                    "적금계좌 잔액 송금"
            ));
        } catch (IOException e) {
            System.out.println("[ERROR] transaction.txt 파일에 저장할 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
            return;
        }
        System.out.println("적금 해지 완료. 금액이 입금되었습니다.");
        List<Transaction> savingsTransactions = transactionRepository.findSavingTransactionByAccount(savingAccount);
        savingsTransactions.forEach(transactionRepository::deleteDepositTransaction);

        savingAccount.deactivate();
        savingAccount.setClosed();
        member.setHasSavingAccount(false);
    }

    private SavingsType selectDepositProduct(Scanner scanner) {
        System.out.println("BanKU: --------------------------------------------------------------------------");
        System.out.println("                                적금        상품        종류                        ");
        System.out.println("      ---------------------------------------------------------------------------");
        System.out.println("      +------------------------+--------------------------+---------------------+");
        System.out.println("      |         상   품         |         약 정 금 리        |    중도 해지 시 금리     ");
        System.out.println("      +------------------------+--------------------------+---------------------+");
        System.out.println("      |  1) 자유 적금 - 06개월    |           2.0%           |         1.0%           ");
        System.out.println("      +------------------------+--------------------------+---------------------+");
        System.out.println("      |  2) 자유 적금 - 12개월    |           3.0%           |         0.5%           ");
        System.out.println("      +------------------------+--------------------------+---------------------+");
        System.out.println("      |  3) 자유 적금 - 18개월    |           4.0%           |         0.1%           ");
        System.out.println("      +------------------------+--------------------------+---------------------+");
        System.out.println();
        while (true) {
            System.out.print("BanKU: 가입을 원하는 상품 번호를 입력해주세요(1~3 사이의 숫자) > ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    return SavingsType.SHORT;
                case "2":
                    return SavingsType.MID;
                case "3":
                    return SavingsType.LONG;
                default:
                    System.out.println("[ERROR] 잘못된 입력입니다. 1~3 사이의 숫자를 입력해주세요. > ");
            }
        }
    }


    private Account chooseReceivingAccount(Scanner scanner, List<Account> regularAccounts) {
        System.out.println("적금 잔액을 입금할 계좌의 계좌번호를 입력해주세요:");
        for (Account account : regularAccounts) {
            System.out.println("- " + account.getAccountNumber());
        }
        while (true) {
            System.out.print("계좌번호 입력 > ");
            String input = scanner.nextLine().trim();
            for (Account account : regularAccounts) {
                if (account.getAccountNumber().equals(input)) {
                    return account;
                }
            }
            System.out.println("[ERROR] 올바른 계좌번호를 입력해주세요.");
        }
    }
}
