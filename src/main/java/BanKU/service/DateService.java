package BanKU.service;

import BanKU.repository.DateRepository;
import BanKU.view.InputView;

import java.io.IOException;
import java.time.MonthDay;
import java.util.Scanner;

import static BanKU.utils.DateValidator.validateDate;

public class DateService {
    private final DateRepository dateRepository;

    public DateService(DateRepository dateRepository) {
        this.dateRepository = dateRepository;
    }

    public MonthDay getNowDate(Scanner scanner) {
        while (true) {
            try {
                MonthDay nowDate = validateDate(InputView.requestNowDate(scanner));
                dateRepository.isAfterLastDate(nowDate);
                dateRepository.save(nowDate);
                return nowDate;
            } catch (IllegalArgumentException | IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
