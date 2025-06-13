package BanKU.repository;

import BanKU.domain.Account;
import BanKU.domain.Reservation;
import BanKU.domain.SavingAccount;
import BanKU.domain.Transaction;
import BanKU.enums.TransactionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static BanKU.Main.RESERVATION_FILE_PATH;

public class ReservationRepository {
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final List<Reservation> reservations = new ArrayList<>();
    List<String> validLines = new ArrayList<>();

    public ReservationRepository(TransactionRepository transactionRepository,MemberRepository memberRepository) {
        this.transactionRepository = transactionRepository;
        this.memberRepository = memberRepository;
        try {
            loadReservationFile();
        } catch (IOException e) {
            System.out.println("[ERROR] reservation.txt 파일을 읽어올 수 없습니다. 프로그램을 종료합니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    private void loadReservationFile() throws IOException{
        Path path = Paths.get(RESERVATION_FILE_PATH);
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            try {
                String[] strings = line.split("\\|");
                Reservation reservation = Reservation.from(strings);
                validLines.add(line);                   // 유효한 행만 저장
                reservations.add(reservation);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        Files.write(path, validLines);          // 유효한 행들만 다시 파일에 덮어쓰기
    }

    public void reservedTransfer(LocalDate now) throws IOException {
        validLines.clear();
        Path path = Paths.get(RESERVATION_FILE_PATH);
        for(Reservation reservation:reservations){
            try {
                if (!reservation.getTransferDate().isAfter(now)) {
                    Transaction reservedDeposit = new Transaction(reservation.getReceiverAccountNumber(),
                            reservation.getTransferDate(),
                            TransactionType.DEPOSIT, reservation.getSenderAccountNumber(),
                            reservation.getAmount(), reservation.getMemo());
                    Transaction reservedWithdrawal = new Transaction(reservation.getSenderAccountNumber(),
                            reservation.getTransferDate(),
                            TransactionType.WITHDRAWAL, reservation.getReceiverAccountNumber(),
                            reservation.getAmount(), reservation.getMemo());
                    Account sendAccount = memberRepository.findAccountByNumber(reservedWithdrawal.getSenderAccountNumber());
                    Account receiverAccount = memberRepository.findAccountByNumber(reservedWithdrawal.getReceiverAccountNumber());
                    if(sendAccount.isActive() && receiverAccount.isActive()){
                        validateTransaction(reservedWithdrawal);
                        validateTransaction(reservedDeposit);
                    }else{
                        throw new IllegalArgumentException();
                    }
                    if(memberRepository.findAccountByNumber(reservation.getReceiverAccountNumber()) instanceof SavingAccount){
                        transactionRepository.saveDeposit(reservedDeposit);
                    }else{
                        transactionRepository.save(reservedDeposit);
                    }
                    transactionRepository.save(reservedWithdrawal);
                } else {
                    String str = getLine(reservation);
                    validLines.add(str);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("[WARNING] 비활성화 계좌의 계좌번호, 혹은 존재하지 않는 계좌 번호로 인하여 누락된 거래 내역이 있습니다.");
            }
        }
        Files.write(path,validLines);
    }

    public String getLine(Reservation reservation){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String str = "";
        str += reservation.getSenderAccountNumber() + "|";
        str += reservation.getTransferDate().format(formatter) + "|";
        str += reservation.getReceiverAccountNumber() + "|";
        try{
            if (reservation.getMemo()!=null||!reservation.getMemo().equals("")) {
                str += reservation.getAmount() + "|";
                str += reservation.getMemo();
            } else {
                str += reservation.getAmount();
            }
        }catch(NullPointerException e){
            str += reservation.getAmount();
        }
        return str;
    }

    public void save(Reservation reservation) throws IOException{
        Path path = Paths.get(RESERVATION_FILE_PATH);
        String str = getLine(reservation);
        validLines.add(str);
        Files.write(path, validLines);
    }

    private void validateTransaction(Transaction transaction) throws IllegalArgumentException {
        Account senderAccount = memberRepository.findAccountByNumber(transaction.getSenderAccountNumber());

        try {
            transaction.applyToAccounts(senderAccount);               // 거래 내역을 계좌 잔액에 반영
        } catch (IllegalArgumentException e) {
            senderAccount.deactivate();
            throw e;
        }
    }
}
