package BanKU.repository;


import BanKU.utils.DateValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static BanKU.Main.DATE_FILE_PATH;

public class DateRepository {

    private final List<MonthDay> dates = new ArrayList<>();

    public DateRepository() {
        try {
            loadDateFile();
        } catch (IOException | URISyntaxException e) {
            System.out.println("[ERROR] date.txt 파일을 읽어올 수 없습니다. 프로그램을 종료합니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    private void loadDateFile() throws IOException, URISyntaxException {
        Path path = Paths.get(DATE_FILE_PATH);
        Files.lines(path)
                .map(DateValidator::validateDate)
                .filter(Objects::nonNull)
                .forEach(this::addDates);
    }

    /**
     * 배열의 맨 마지막 원소가 현재 행(monthDay)보다 과거여야만 monthDay를 dates 배열에 추가한다.
     * 이에 부합하지 않는 경우 현재 행을 무시한다.
     */
    private void addDates(MonthDay monthDay) {
        if (dates.isEmpty()) {
            dates.add(monthDay);
//            System.out.println(monthDay.toString());
        } else if (dates.get(dates.size() - 1).isBefore(monthDay)) {
            dates.add(monthDay);
//            System.out.println(monthDay.toString());
        }
    }

    public MonthDay isAfterLastDate(MonthDay nowDate) {
        if (dates.isEmpty()){
            dates.add(nowDate);      // date.txt 파일이 비어있는 경우 모든 날짜가 가능하도록 처리
            return nowDate;
        }
        MonthDay lastDate = dates.get(dates.size() - 1);
        if (!lastDate.isBefore(nowDate)){
            throw new IllegalArgumentException("[ERROR] "+lastDate +"보다 이후의 날짜여야 합니다. \n 현재 날짜를 다시 입력해주세요.");
        }
        return nowDate;
    }

    public void save(MonthDay now) {
        // TODO. 날짜 파일에 오늘 날짜 저장
    }
}
