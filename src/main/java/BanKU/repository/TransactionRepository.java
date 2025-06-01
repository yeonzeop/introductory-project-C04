package BanKU.repository;

import BanKU.domain.Account;
import BanKU.domain.SavingAccount;
import BanKU.domain.Transaction;
import BanKU.enums.TransactionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static BanKU.Main.DEPOSIT_TRANSACTION_FILE_PATH;
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
        boolean isValidDate = !transactions.get(transactions.size() - 1)
                .getDate()
                .isAfter(transaction.getDate());
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
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

    public void loadDepositTransactionFile() {
        // TODO. 적금 거래내역 파일을 불러와 회차별 거래 내역들을 저장해둠.
        // 기존 loadTransactionFile의 로직과 매우 유사함.
        // file path가 DEPOSIT_TRANSACTION_FILE_PATH로 바뀌기만함
        Path path = Paths.get("data/deposit_transaction.txt"); // DEPOSIT_TRANSACTION_FILE_PATH가 정의되어 있다면 상수로 대체
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                try {
                    String[] strings = line.split("\\|");
                    Transaction transaction = Transaction.from(strings);
                    validateDate(transaction);
                    validateTransaction(transaction);
                    validLines.add(line);
                    transactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            Files.write(path, validLines);
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 거래내역 파일을 읽어올 수 없습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    public List<Transaction> findTransactionByAccount(SavingAccount savingAccount) {
        // TODO. 해지시 이자 계산을 위해 사용
        // Parameter로 들어온 적금 계좌가 가지는 거래내역들을 모두 저장하여, 저장된 거래내역을 리턴함
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getSenderAccountNumber().equals(savingAccount.getAccountNumber())) {
                result.add(transaction);
            }
        }
        return result;
    }

    public void saveDeposit(Transaction transaction) throws IOException {
        // TODO. 적금 거래내역 파일에 거래내역을 쓰는 함수
        // Parameter로 들어온 적금 거래내역들을 모두 저장함.
        // 기존의 save함수에서 path를 DEPOSIT_TRANSACTION_FILE_PATH로 수정하고, 동일 로직으로 적금 거래내역을 저장함.
        Path path = Paths.get("data/deposit_transaction.txt"); // DEPOSIT_TRANSACTION_FILE_PATH가 정의되어 있다면 상수로 대체
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String str = getString(transaction, formatter);
        validLines.add(str);
        Files.write(path, validLines);
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
}
