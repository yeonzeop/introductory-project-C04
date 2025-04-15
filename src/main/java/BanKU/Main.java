package BanKU;


import BanKU.controller.BanKUController;
import BanKU.domain.Account;
import BanKU.repository.DateRepository;
import BanKU.repository.MemberRepository;
import BanKU.repository.TransactionRepository;
import BanKU.view.InputView;

public class Main {

    public static final String DATE_FILE_PATH = "src/main/resources/date.txt";
    public static final String USER_FILE_PATH = "src/main/resources/user.txt";
    public static final String TRANSACTION_FILE_PATH = "src/main/resources/transaction.txt";

    public static void main(String[] args) {
        // 1. 파일 읽어서 메모리에 로드
        DateRepository dateRepository = new DateRepository();           // date.txt 파일 읽어오기
        MemberRepository memberRepository = new MemberRepository();     // user.txt 파일 읽어오기
        TransactionRepository transactionRepository = new TransactionRepository(memberRepository);      // transaction.txt 파일 읽어오기

//        transactionRepository.printTransactions();
//        memberRepository.printAccounts();

        // 2. 메인 기능
        BanKUController banKUController = new BanKUController(dateRepository, memberRepository, transactionRepository);
        banKUController.run();
    }
}
