package BanKU.repository;


import BanKU.domain.Transaction;
import BanKU.utils.DateValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

import static BanKU.Main.DATE_FILE_PATH;

public class DateRepository {

    private final List<LocalDate> dates = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    public DateRepository(MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        try {
            loadDateFile();
        } catch (IOException | URISyntaxException e) {
            System.out.println("[ERROR] date.txt 파일을 읽어올 수 없습니다. 프로그램을 종료합니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
            System.exit(1); // 비정상 종료
        } catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
            System.exit(1); // 비정상 종료
        }
    }

    private void loadDateFile() throws IOException, URISyntaxException {
        Path path = Paths.get(DATE_FILE_PATH);
        Files.lines(path)
                .map(this::safeValidateDate)
                .filter(Objects::nonNull)
                .forEach(this::addDates);
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(transactionRepository.getRegularTransactions());
        allTransactions.addAll(transactionRepository.getSavingTransactions());
        allTransactions.sort(Comparator.comparing(Transaction::getDate));
        if (!allTransactions.isEmpty() && !dates.isEmpty()) {
            Transaction lastTransaction = allTransactions.get(allTransactions.size() - 1);
            LocalDate lastDateInFile = dates.get(dates.size() - 1);
            if (lastTransaction.getDate().isAfter(lastDateInFile)) {
                throw new IllegalArgumentException("[ERROR] 날짜 파일에 이상을 감지하여 프로그램을 종료합니다.");
            }
        }
    }

    private LocalDate safeValidateDate(String line) {
        try {
            return DateValidator.validateDate(line);
        } catch (Exception e) {
//            System.out.println("[WARNING] 잘못된 날짜 형식: " + line);
            return null;
        }
    }

    /**
     * 배열의 맨 마지막 원소가 현재 행(monthDay)보다 과거여야만 monthDay를 dates 배열에 추가한다.
     * 이에 부합하지 않는 경우 현재 행을 무시한다.
     */
    private void addDates(LocalDate localDate) {
        if (dates.isEmpty()) {
            dates.add(localDate);
        } else if (dates.get(dates.size() - 1).isBefore(localDate)) {
            dates.add(localDate);
        }
    }

    public void isAfterLastDate(LocalDate nowDate) {
        if (dates.isEmpty()) {
            return;      // date.txt 파일이 비어있는 경우 모든 날짜가 가능하도록 처리
        }
        LocalDate lastDate = dates.get(dates.size() - 1);
        if (!lastDate.isBefore(nowDate)) {
            throw new IllegalArgumentException("[ERROR] " + lastDate.format(formatter) + " 보다 이후의 날짜여야 합니다. 현재 날짜를 다시 입력해주세요.");
        }
        long diffMonths = ChronoUnit.MONTHS.between(nowDate,dates.get(dates.size() - 1));
        if( diffMonths > 0){
            memberRepository.freeAccountInterest(diffMonths);
        }
    }

    public void save(LocalDate now) throws IOException {
        Path path = Paths.get(DATE_FILE_PATH);
        dates.add(now);
        List<String> rawDates = new ArrayList<>();
        for(LocalDate date:dates){
            String str = date.format(formatter);
            rawDates.add(str);
        }
        Files.write(path,rawDates);
    }

    public LocalDate getNow() {
        return dates.get(dates.size() - 1);
    }
}
