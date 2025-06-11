package BanKU;


import BanKU.controller.BanKUController;
import BanKU.repository.DateRepository;
import BanKU.repository.MemberRepository;
import BanKU.repository.ReservationRepository;
import BanKU.repository.TransactionRepository;

public class Main {

    public static final String DATE_FILE_PATH = "src/main/resources/date.txt";
    public static final String USER_FILE_PATH = "src/main/resources/user.txt";
    public static final String TRANSACTION_FILE_PATH = "src/main/resources/transaction.txt";
    public static final String RESERVATION_FILE_PATH = "src/main/resources/reservation.txt";
    public static final String DEPOSIT_INFO_FILE_PATH = "src/main/resources/depositInfo.txt";
    public static final String DEPOSIT_TRANSACTION_FILE_PATH = "src/main/resources/depositTransaction.txt";

    public static void main(String[] args) {
        // 1. 파일 읽어서 메모리에 로드
        MemberRepository memberRepository = new MemberRepository();     // user.txt 파일 읽어오기
        TransactionRepository transactionRepository = new TransactionRepository(memberRepository);      // transaction.txt 파일 읽어오기
        DateRepository dateRepository = new DateRepository(memberRepository, transactionRepository);           // date.txt 파일 읽어오기
        ReservationRepository reservationRepository = new ReservationRepository(transactionRepository, memberRepository);

//        transactionRepository.printTransactions();
//        memberRepository.printAccounts();

        // 2. 메인 기능
        BanKUController banKUController = new BanKUController(dateRepository, memberRepository, transactionRepository, reservationRepository);
        banKUController.run();
    }
}
