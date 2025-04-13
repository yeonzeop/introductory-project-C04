package BanKU.service;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.repository.TransactionRepository;
import BanKU.view.InputView;
import BanKU.view.OutputView;

import java.time.MonthDay;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class AccountService {
    private MonthDay now;
    private final TransactionRepository transactionRepository;

    public AccountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void deposit(Account account) {
        transactionRepository.saveDeposit();
    }

    public Account choose(Member member, Scanner scanner) {
        List<Account> accounts = member.getAccounts();
        OutputView.showAccounts(member.getName(), accounts);
        return InputView.chooseAccount(accounts, scanner);
    }

    public void withdrawal(Account account) {
        transactionRepository.sageWithdrawal();
    }

    public void transfer(Account account) {
        transactionRepository.saveDeposit();
        transactionRepository.sageWithdrawal();
    }

    public void setNow(MonthDay now) {
        this.now = now;
    }

}
