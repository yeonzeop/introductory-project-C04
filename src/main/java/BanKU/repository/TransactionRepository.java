package BanKU.repository;

import BanKU.domain.Account;
import BanKU.domain.Transaction;
import BanKU.enums.TransactionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static BanKU.Main.TRANSACTION_FILE_PATH;


public class TransactionRepository {
    private final MemberRepository memberRepository;
    private final List<Transaction> transactions = new ArrayList<>();
    List<String> validLines = new ArrayList<>();


    public TransactionRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        try {
            loadTransactionFile();
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
                validateDate(transaction);
                validateTransaction(transaction);       // 계좌 잔액에 반영
                validLines.add(line);                   // 유효한 행만 저장
                transactions.add(transaction);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        Files.write(path, validLines);          // 유효한 행들만 다시 파일에 덮어쓰기
    }

    private void validateDate(Transaction transaction) {
        // 첫 번째 거래는 무조건 유효
        if (transactions.isEmpty()) {
            return;
        }
        boolean isValidDate = transactions.get(transactions.size() - 1)
                .getDate()
                .isBefore(transaction.getDate());
        if (!isValidDate) {
            throw new IllegalArgumentException("[WARNING] 거래내역 데이터에 손실이 있습니다. 해당 행을 무시합니다. 날짜: " + transaction.getDate());
        }
    }

    private void validateTransaction(Transaction transaction) throws IllegalArgumentException {
        Account senderAccount = memberRepository.findAccountByNumber(transaction.getSenderAccountNumber());
        Account receiverAccount = memberRepository.findAccountByNumber(transaction.getReceiverAccountNumber());

        try {
            transaction.applyToAccounts(senderAccount);               // 거래 내역을 계좌 잔액에 반영
        } catch (IllegalArgumentException e) {
            if (transaction.getType() == TransactionType.DEPOSIT) {
                senderAccount.deactivate();
            } else {
                senderAccount.deactivate();
            }
            System.out.println(e.getMessage());
        }
    }


    public void save(Transaction transaction) throws IOException {
        Path path = Paths.get(TRANSACTION_FILE_PATH);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        String str = getString(transaction, formatter);
        validLines.add(str);
        Files.write(path, validLines);
    }

    private static String getString(Transaction transaction, DateTimeFormatter formatter) {
        String str = "";
        str += transaction.getSenderAccountNumber() + "|";
        str += transaction.getDate().format(formatter) + "|";
        str += transaction.getType().toString() + "|";
        str += transaction.getReceiverAccountNumber() + "|";
        if (!transaction.getMemo().equals("")) {
            str += transaction.getAmount() + "|";
            str += transaction.getMemo();
        } else {
            str += transaction.getAmount();
        }
        return str;
    }

    public void printTransactions() {
        System.out.println("[TransactionRepository]");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }
        System.out.println();
    }
}
