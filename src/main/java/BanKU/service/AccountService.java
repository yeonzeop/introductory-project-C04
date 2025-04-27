package BanKU.service;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.domain.Transaction;
import BanKU.enums.TransactionType;
import BanKU.repository.MemberRepository;
import BanKU.repository.TransactionRepository;
import BanKU.view.InputView;
import BanKU.view.OutputView;

import java.io.IOException;
import java.time.MonthDay;
import java.util.List;
import java.util.Scanner;

import static BanKU.enums.TransactionType.*;
import static BanKU.utils.TransactionValidator.AMOUNT_LIMIT;
import static BanKU.utils.TransactionValidator.validateAccountNumber;

public class AccountService {
    private MonthDay now;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account choose(Member member, Scanner scanner) {
        List<Account> accounts = member.getAccounts();
        System.out.println("[" + member.getName() + "]님의 사용가능한 계좌 목록");
        OutputView.showAccounts(accounts);
        return InputView.chooseAccount(accounts, scanner);
    }

    public void deposit(Account account, Scanner scanner) {
        if (!account.isActive()) {
            System.out.println("[ERROR] 비활성화된 계좌에 입금할 수 없습니다.");
            return;
        }
        try {
            long amount = getAmount(account, scanner, DEPOSIT);
            Transaction transaction = new Transaction(
                    account.getAccountNumber(),
                    now,
                    DEPOSIT,
                    account.getAccountNumber(),
                    amount,
                    "");
            if (account.getBalance() > Long.MAX_VALUE - amount) {
                System.out.println("[ERROR] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
                account.deactivate();
                return;
            }
            checkAccountPassword(account, scanner);
            if (!applyTransactionToAccount(account, transaction)) {
                return;
            }
            transactionRepository.save(transaction);
            System.out.println("BanKU: 입금이 완료되었습니다.\n" +
                    "       입금한 계좌(사용자 계좌): " + account.getAccountNumber() + " 잔액(단위: 원): " + account.getBalance() + "원");
        } catch (IOException e) {
            System.out.println("[ERROR] transaction.txt 파일에 저장할 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }


    public void withdrawal(Account account, Scanner scanner) {
        if (!account.isActive()) {
            System.out.println("[ERROR] 비활성화된 계좌에서 출금할 수 없습니다.");
            return;
        }
        try {
            long amount = getAmount(account, scanner, WITHDRAWAL);
            Transaction transaction = new Transaction(
                    account.getAccountNumber(),
                    now,
                    WITHDRAWAL,
                    account.getAccountNumber(),
                    amount,
                    "");
            if (account.getBalance() < amount) {
                System.out.println("[ERROR] 계좌 잔액 문제가 발생하여 계좌를 비활성화 합니다.");
                account.deactivate();
                return;
            }
            checkAccountPassword(account, scanner);

            transactionRepository.save(transaction);
            if (!applyTransactionToAccount(account, transaction)) {
                return;
            }
            System.out.println("BanKU: 출금이 완료되었습니다.\n" +
                    "       출금한 계좌(사용자 계좌): " + account.getAccountNumber() + " 잔액(단위: 원): " + account.getBalance() + "원");
        } catch (IOException e) {
            System.out.println("[ERROR] transaction.txt 파일에 저장할 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    private boolean applyTransactionToAccount(Account account, Transaction transaction) {
        try {
            transaction.applyToAccounts(account);
            return true;
        } catch (IllegalArgumentException e) {
            account.deactivate();
            System.out.println(e.getMessage());
            return false;
        }
    }

    private long getAmount(Account account, Scanner scanner, TransactionType type) {
        boolean isFirstPrint = true;
        long amount;
        while (true) {
            String rawAmount = InputView.requestAmount(account, scanner, type, isFirstPrint).trim();
            isFirstPrint = false;
            if (!rawAmount.matches("\\d+")) {
                System.out.println("[ERROR] 금액은 숫자로만 입력 가능합니다. 원 단위에 맞춰 금액을 다시 입력해주세요.");
                continue;
            }
            try {
                amount = Long.parseLong(rawAmount);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] 거래 한도 10,000,000 원(일천만 원)보다 더 큰 금액은 거래가 불가능합니다.");
                continue;
            }
            if (amount > AMOUNT_LIMIT) {
                System.out.println("[ERROR] 거래 한도 10,000,000 원(일천만 원)보다 더 큰 금액은 거래가 불가능합니다.");
                continue;
            }
            if (amount <= 0) {
                System.out.println("[ERROR] 거래 금액은 양수여야 합니다.");
                continue;
            }
            return amount;
        }
    }

    private void checkAccountPassword(Account account, Scanner scanner) {
        while (true) {
            System.out.print("BanKU: 계좌 비밀번호를 입력해주세요(숫자 4자리로 입력해주세요) > ");
            String password = scanner.nextLine().trim();
            if (!password.matches("\\d{4}")) {
                System.out.println("[ERROR] 계좌 비밀번호는 숫자로만 입력 가능합니다. 비밀번호를 다시 입력해주세요.");
                continue;
            }
            if (!account.getPassword().equals(password)) {
                System.out.println("[ERROR] 계좌 비밀번호가 틀렸습니다. 비밀번호를 다시 입력해주세요.");
                continue;
            }
            break;
        }
    }

    public void transfer(Account senderAccount, Scanner scanner) {
        if (!senderAccount.isActive()) {
            System.out.println("[ERROR] 비활성화된 계좌에서 송금할 수 없습니다.");
            return;
        }
        try {
            boolean isFirstPrint = true;
            Account receiverAccount = getReceiverAccount(senderAccount, scanner, isFirstPrint);
            long amount = getAmount(senderAccount, scanner, TRANSFER);
            if (receiverAccount.getBalance() > Long.MAX_VALUE - amount) {
                System.out.println("BanKU: 계좌 잔액 문제가 발생하여 해당 계좌를 비활성화 합니다. [비활성 계좌: " + receiverAccount.getAccountNumber() + "]");
                receiverAccount.deactivate();
                return;
            }
            checkAccountPassword(senderAccount, scanner);
            String memo = requestMemo(scanner);

            Transaction receiveTransaction = new Transaction(
                    receiverAccount.getAccountNumber(),
                    now,
                    DEPOSIT,
                    senderAccount.getAccountNumber(),
                    amount,
                    memo);
            Transaction sendTransaction = new Transaction(
                    senderAccount.getAccountNumber(),
                    now,
                    WITHDRAWAL,
                    receiverAccount.getAccountNumber(),
                    amount,
                    memo);
            boolean applyReceive = applyTransactionToAccount(receiverAccount, receiveTransaction);
            boolean applySend = applyTransactionToAccount(senderAccount, sendTransaction);
            if (!(applyReceive && applySend)) {
                return;
            }
            transactionRepository.save(receiveTransaction);
            transactionRepository.save(sendTransaction);

            System.out.println("BanKU: 송금이 완료되었습니다.\n" +
                    "       송금한 계좌(사용자 계좌): " + senderAccount.getAccountNumber() + " 잔액(단위: 원): " + senderAccount.getBalance() + "원");
        } catch (IOException e) {
            System.out.println("[ERROR] transaction.txt 파일에 저장할 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }

    }

    private Account getReceiverAccount(Account senderAccount, Scanner scanner, boolean isFirstPrint) {
        while (true) {
            String accountNumber = InputView.requestReceiverAccount(senderAccount, scanner, isFirstPrint);
            if (!accountNumber.matches("\\d{12}")) {
                System.out.println("[ERROR] 계좌 번호는 -없이 숫자로만 입력가능합니다. 다시 입력해주세요.");
                continue;
            }
            try {
                return memberRepository.findAccountByNumber(accountNumber);
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] 존재하지 않는 계좌번호입니다. 다시 입력해주세요.");
            }
        }
    }

    private String requestMemo(Scanner scanner) {
        String memo;
        while (true) {
            System.out.print("BanKU: 송금 시 기록할 메모를 남겨주세요 (10자 이내) > ");
            memo = scanner.nextLine();
            if (memo.matches(".*[|%&].*")) {
                System.out.println("[ERROR] 메모에 구분자가 포함될 수 없습니다. 다시 입력해주세요.");
                continue;
            }
            if (memo.length() > 10) {
                return memo.substring(0, 10); // 10자까지만 자르기
            }
            return memo;
        }
    }

    public void setNow(MonthDay now) {
        this.now = now;
    }

}
