package BanKU.service;

import BanKU.repository.DateRepository;
import BanKU.view.InputView;

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
                return dateRepository.isAfterLastDate(nowDate);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
