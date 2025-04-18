package BanKU.service;

import BanKU.repository.DateRepository;
import BanKU.view.InputView;

import java.io.IOException;
import java.time.MonthDay;
import java.util.Scanner;

public class DateService {
    private final DateRepository dateRepository;

    public DateService(DateRepository dateRepository) {
        this.dateRepository = dateRepository;
    }

    public MonthDay getNowDate(Scanner scanner) {
        while (true) {
            try {
                MonthDay nowDate = InputView.requestNowDate(scanner);
                MonthDay now = dateRepository.isAfterLastDate(nowDate);
                dateRepository.save(now);
                return now;
            } catch (IllegalArgumentException | IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
