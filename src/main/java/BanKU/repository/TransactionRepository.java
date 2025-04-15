package BanKU.repository;

import BanKU.domain.Account;
import BanKU.domain.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            System.out.println("[WARNING] 거래내역 데이터에 손실이 있습니다. 해당 행을 무시합니다. 날짜: " + transaction.getDate());
        }
    }

    private void validateTransaction(Transaction transaction) throws IllegalArgumentException {
        Account senderAccount = memberRepository.findAccountByNumber(transaction.getSenderAccountNumber());
        Account receiverAccount = memberRepository.findAccountByNumber(transaction.getReceiverAccountNumber());

        // TODO. Q. 비활성 계좌는 member의 account 에서도 제거해야하나?? 아직은 삭제 X
        // TODO. Q. 모든 거래내역에 대해서 (입금, 출금 모두) receiverAccount 에만 잔액이 수정되도록 설정해둠
        transaction.applyToAccounts(receiverAccount);               // 거래 내역을 계좌 잔액에 반영

        // 비활성화 상태가 된 계좌 처리 : 이미 처리된 거래 내역 -> 그냥 냅둠 (유효)
        //                         이후에 처리할 거래 내역 -> 무시 & 파일에서 삭제

    }



    public void save(Transaction transaction) {
        // TODO. transaction.txt 파일에 transaction 추가하기
    }

    public void printTransactions() {
        System.out.println("[TransactionRepository]");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }
        System.out.println();
    }
}
