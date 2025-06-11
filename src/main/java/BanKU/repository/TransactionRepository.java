package BanKU.repository;

import BanKU.domain.Account;
import BanKU.domain.SavingAccount;
import BanKU.domain.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static BanKU.Main.DEPOSIT_TRANSACTION_FILE_PATH;
import static BanKU.Main.TRANSACTION_FILE_PATH;


public class TransactionRepository {
    private final MemberRepository memberRepository;
    private final List<Transaction> regularTransactions = new ArrayList<>();
    private final List<Transaction> savingTransactions = new ArrayList<>();
    List<String> validRegularTransactionLines = new ArrayList<>();
    List<String> validSavingTransactionLines = new ArrayList<>();

    public TransactionRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        try {
            loadTransactionFile();
            loadDepositTransactionFile();
        } catch (IOException e) {
            System.out.println("[ERROR] transaction.txt 파일을 읽어올 수 없습니다. 프로그램을 종료합니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    private void loadTransactionFile() throws IOException {
        Path path = Paths.get(TRANSACTION_FILE_PATH);
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            try {
                String[] strings = line.split("\\|");
                Transaction transaction = Transaction.from(strings);
                validateDate(regularTransactions, transaction);
                makeInterest(regularTransactions,transaction);
                validateTransaction(transaction);       // 계좌 잔액에 반영
                validRegularTransactionLines.add(line);                   // 유효한 행만 저장
                regularTransactions.add(transaction);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        Files.write(path, validRegularTransactionLines);          // 유효한 행들만 다시 파일에 덮어쓰기
    }

    private void validateDate(List<Transaction> transactions, Transaction transaction) {
        // 첫 번째 거래는 무조건 유효
        if (transactions.isEmpty()) {
            return;
        }
        boolean isValidDate = !transactions.get(transactions.size() - 1)
                .getDate()
                .isAfter(transaction.getDate());
        if (!isValidDate) {
//            System.out.println();
//            System.out.println("직전 거래내역 날짜 = " + transactions.get(transactions.size() - 1).getDate());
//            System.out.println("현재 거래내역 날짜 = " + transaction.getDate());
            throw new IllegalArgumentException("[WARNING] 거래내역 데이터에 손실이 있습니다. 해당 행을 무시합니다. 날짜: " + transaction.getDate());
        }
    }

    private void validateTransaction(Transaction transaction) throws IllegalArgumentException {
        Account senderAccount = memberRepository.findAccountByNumber(transaction.getSenderAccountNumber());

        try {
            transaction.applyToAccounts(senderAccount);               // 거래 내역을 계좌 잔액에 반영
        } catch (IllegalArgumentException e) {
            senderAccount.deactivate();
            System.out.println(e.getMessage());
        }
    }


    public void save(Transaction transaction) throws IOException {
//        System.out.println("[save LOG] 거래내역 파일에 저장 = "+ transaction.toString());
        Path path = Paths.get(TRANSACTION_FILE_PATH);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String str = getString(transaction, formatter);
        validRegularTransactionLines.add(str);
        regularTransactions.add(transaction);
        Files.write(path, validRegularTransactionLines);
    }

    private static String getString(Transaction transaction, DateTimeFormatter formatter) {
        String str = "";
        str += transaction.getSenderAccountNumber() + "|";
        str += transaction.getDate().format(formatter) + "|";
        str += transaction.getType().toString() + "|";
        str += transaction.getReceiverAccountNumber() + "|";
        try{
        if (!transaction.getMemo().equals("")) {
            str += transaction.getAmount() + "|";
            str += transaction.getMemo();
        } else {
            str += transaction.getAmount();
        }}catch(NullPointerException e){
            str += transaction.getAmount();
        }

        return str;
    }

    public void loadDepositTransactionFile() {
        Path path = Paths.get(DEPOSIT_TRANSACTION_FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                try {
                    String[] strings = line.split("\\|");
                    Transaction transaction = Transaction.from(strings);
                    Account savingAccount = memberRepository.findAccountByNumber((transaction.getSenderAccountNumber()));
                    if (! (savingAccount instanceof SavingAccount)) {
                        throw new IllegalArgumentException("[WARNING] 적금 거래내역 파일의 입금은 적금 계좌에 한해 허용됩니다. 해당 조건을 만족하지 않는 거래 내역은 자동으로 비활성화됩니다.");
                    }
                    validateDate(savingTransactions, transaction);
                    validateTransaction(transaction);
                    validSavingTransactionLines.add(line);
                    savingTransactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            Files.write(path, validSavingTransactionLines);
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 거래내역 파일을 읽어올 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    public List<Transaction> findSavingTransactionByAccount(SavingAccount savingAccount) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : savingTransactions) {
            if (transaction.getSenderAccountNumber().equals(savingAccount.getAccountNumber())) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> findTransactionByAccount(Account account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : regularTransactions) {
            if (transaction.getSenderAccountNumber().equals(account.getAccountNumber())) {
                result.add(transaction);
            }
        }
        return result;
    }

    // TODO. 적금 계좌로 송금 시 추가 (memberService.transfer 메서드에서 적금 계좌인 경우 사용하도록 구현해야됨)
    public void saveDeposit(Transaction transaction) throws IOException {
        Path path = Paths.get(DEPOSIT_TRANSACTION_FILE_PATH);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String str = getString(transaction, formatter);
        validSavingTransactionLines.add(str);
        savingTransactions.add(transaction);
        Files.write(path, validSavingTransactionLines);
    }


    public void deleteDepositTransaction(Transaction transaction) {
        Path path = Paths.get(DEPOSIT_TRANSACTION_FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            String target = getString(transaction, formatter);
            lines.removeIf(line -> line.equals(target));
            Files.write(path, lines);
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 거래내역 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    public List<Transaction> getRegularTransactions() {
        return regularTransactions;
    }

    public List<Transaction> getSavingTransactions() {
        return savingTransactions;
    }

    public long getTotalDepositAmount(String accountNumber, LocalDate date) {
        return savingTransactions.stream()
                .filter(transaction -> transaction.getSenderAccountNumber().equals(accountNumber))
                .filter(transaction -> transaction.getDate().isEqual(date))
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    private void makeInterest(List<Transaction> regularTransactions, Transaction transaction) {
        if (!regularTransactions.isEmpty()) {
            LocalDate last = regularTransactions.get(regularTransactions.size() - 1).getDate();
            LocalDate current = transaction.getDate();
            LocalDate lastStd = LocalDate.of(last.getYear(),last.getMonth(),1);
            LocalDate currentStd = LocalDate.of(current.getYear(),current.getMonth(),1);
            long diffMonths = ChronoUnit.MONTHS.between(lastStd,currentStd);
            if (diffMonths > 0) {
                memberRepository.freeAccountInterest(diffMonths);
            }
        }
    }
}
