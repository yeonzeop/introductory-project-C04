package BanKU.repository;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.domain.SavingAccount;
import BanKU.domain.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static BanKU.Main.DEPOSIT_INFO_FILE_PATH;
import static BanKU.Main.USER_FILE_PATH;

public class MemberRepository {
    private final Map<String, Member> membersByLoginId = new HashMap<>();           // loginId - Member
    private final Map<String, Member> membersByPhoneNumber = new HashMap<>();       // phoneNumber - Member
    private final Map<String, Account> accounts = new HashMap<>();      // 계좌번호 - Account

    public MemberRepository() {
        try {
            loadUserFile();
            loadSavingsFile();
        } catch (IOException e) {
            System.out.println("[ERROR] user.txt 파일을 읽어올 수 없습니다. 프로그램을 종료합니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    private void loadUserFile() throws IOException {
        Path path = Paths.get(USER_FILE_PATH);
        Files.lines(path).map(line -> line.split("\\|")).map(Member::from).filter(Objects::nonNull).forEach(member -> {
            addMember(member);
            addAccount(member.getAccounts());
        });
    }

    private void loadSavingsFile() throws IOException {
        Path path = Paths.get(DEPOSIT_INFO_FILE_PATH);
        validateSavingsFile(path);      // 잘못된 부분이 있으면 수정

        // 수정된 파일을 다시 읽어 로드

//        Files.lines(path)
//                .map(String::trim)
//                .filter(line -> !line.isEmpty())
//                .map(line -> line.split("\\|"))
//                .map(strings -> Map.entry(strings[0], SavingAccount.from(strings)))
//                .filter(entry -> entry.getValue() != null)
//                .forEach(entry -> {
//                    try {
//                        Member member = findMemberByLoginId(entry.getKey().trim());
//                        SavingAccount savingAccount = entry.getValue();
//                        if (savingAccount.isClosed()) {
//                            savingAccount.deactivate();
//                        }
//                        member.addAccount(savingAccount);
//                        accounts.put(savingAccount.getAccountNumber(), savingAccount);
//                    } catch (IllegalArgumentException e) {
//                        System.out.println(e.getMessage());
//                    }
//                });

        Files.lines(path)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(line -> {
            String[] tokens = line.split("\\|");
            try {
                SavingAccount savingAccount = SavingAccount.from(tokens);
                if (savingAccount == null) return;
                Member member = findMemberByLoginId(tokens[0].trim());
                if (savingAccount.isClosed()) {
                    savingAccount.deactivate();
                }
                member.addAccount(savingAccount);
                accounts.put(savingAccount.getAccountNumber(), savingAccount);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private void validateSavingsFile(Path path) throws IOException {
        Map<String, Boolean> hasOpenedAccount = new HashMap<>();     // 사용자별 opened 계좌가 하나만 있도록 하기 위한 상태 추적용 Map
        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            String[] tokens = trimmedLine.split("\\|");
            String userId = tokens[0];
            String status = tokens[5];

            // 사용자별로 이미 opened 계좌가 있는지 확인
            boolean alreadyOpened = hasOpenedAccount.getOrDefault(userId, false);

            // opened 계좌가 1개 이상이면 강제로 closed 처리
            if ("opened".equalsIgnoreCase(status)) {
                if (!alreadyOpened) {
                    hasOpenedAccount.put(userId, true);
                } else {
                    System.out.println("[ERROR] 적금 계좌는 사용자당 1개까지만 허용됩니다. 중복된 계좌가 확인되어 프로그램을 종료합니다.");
                    System.exit(1);
                }
            }
        }
    }


    private void addMember(Member member) {
        boolean alreadyExistLoginId = membersByLoginId.containsKey(member.getLoginId());
        boolean alreadyExistPhoneNumber = membersByPhoneNumber.containsKey(member.getLoginId());
        if (alreadyExistLoginId) {
            System.out.println("[WARNING] 중복된 아이디 기재로 인하여 누락된 회원 정보가 있습니다.");
            return;
        }
        if (alreadyExistPhoneNumber) {
            System.out.println("[WARNING] 중복된 전화번호 기재로 인하여 누락된 회원 정보가 있습니다.");
            return;
        }
        membersByLoginId.put(member.getLoginId(), member);
        membersByPhoneNumber.put(member.getPhoneNumber(), member);
    }

    private void addAccount(List<Account> accounts) {
        for (Account account : accounts) {
            this.accounts.put(account.getAccountNumber(), account);
        }
    }

    public void saveMember(Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String newLine = member.getLoginId().toLowerCase() + "|" + member.getPassword() + "|" + member.getName() + "|" + member.getBirthday().format(formatter) + "|" + member.getPhoneNumber() + "|";


        Path path = Paths.get(USER_FILE_PATH);
        try {
            List<String> originalLines = Files.readAllLines(path);
            originalLines.add(0, newLine); // 새 줄을 맨 위에 삽입

            Files.write(path, originalLines); // 전체 내용 다시 파일에 쓰기
        } catch (IOException e) {
            System.out.println("[ERROR] 회원 정보를 파일에 저장하는 데 실패했습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }

    }


    public void saveAccount(Member member, Account account) {
        Path path = Paths.get(USER_FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\|");

                if (parts[0].equals(member.getLoginId().toLowerCase())) {

                    String newAccountStr;
                    if (parts.length < 6) {
                        newAccountStr = account.getAccountNumber() + "&" + account.getPassword();
                    } else {
                        newAccountStr = "%" + account.getAccountNumber() + "&" + account.getPassword();
                    }
                    // 새로 갱신된 라인 만들기

                    lines.set(i, line + newAccountStr);
                    break;
                }
            }

            Files.write(path, lines);
        } catch (IOException e) {
            System.out.println("[ERROR] 계좌 정보를 파일에 저장하는 데 실패했습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    public boolean isPresentAccount(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    public Account findAccountByNumber(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null || (!(account instanceof SavingAccount) && !account.isActive())) {
//            System.out.println("[findAccountByNumber LOG] 계좌번호 = " + accountNumber + ", 적금계좌인가? " + (account instanceof SavingAccount));
            throw new IllegalArgumentException("[WARNING] 비활성화 계좌의 계좌번호, 혹은 존재하지 않는 계좌 번호로 인하여 누락된 거래 내역이 있습니다.");
        }
        return account;
    }

    public Member findMemberByLoginId(String loginId) {
        return membersByLoginId.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(loginId)).map(Map.Entry::getValue).findFirst().orElseThrow(() -> new IllegalArgumentException("[ERROR] 등록되지 않은 아이디입니다."));
    }

    public boolean isExistingLoginId(String loginId) {
        return membersByLoginId.containsKey(loginId);
    }


    // 로깅용 임시 메서드
    public void printAccounts() {
        System.out.println("[printAccounts]");
        for (Account account : accounts.values()) {
            System.out.println(account.toString());
        }
        System.out.println();
    }

    public void saveSavingsAccount(Member member, SavingAccount savingAccount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEPOSIT_INFO_FILE_PATH, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(member.getLoginId().toLowerCase()).append("|").append(savingAccount.getAccountNumber()).append("|").append(savingAccount.getPassword()).append("|").append(savingAccount.getStartDay().format(formatter)).append("|").append(savingAccount.getEndDay().format(formatter)).append("|").append(savingAccount.isClosed() ? "closed" : "opened");
            System.out.println("[saveSavingsAccount LOG] 적금계좌 저장 형태 = " + sb.toString());
            writer.write(sb.toString());
            writer.newLine();
            accounts.put(savingAccount.getAccountNumber(), savingAccount);
            System.out.println("[saveSavingsAccount LOG] 새로 생성한 계좌 = " + savingAccount.toString());
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 계좌 정보를 파일에 저장하는 데 실패했습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    public void setSavingsAccountClosed(Member member, SavingAccount account) {
        Path path = Paths.get(DEPOSIT_INFO_FILE_PATH);

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.contains(account.getAccountNumber()) && line.endsWith("|opened")) {
                    updatedLines.add(line.replace("|opened", "|closed"));
                } else {
                    updatedLines.add(line);
                }
            }

            Files.write(path, updatedLines);
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 계좌 정보를 파일을 변경하는 데 실패했습니다.");
        }
    }

    public void freeAccountInterest(long diffMonths) {
        double interestRate = 0.1;
        for (Account account : accounts.values()) {
            if (account.isActive() && !(account instanceof SavingAccount)) {
                long interest = (long) Math.ceil(account.getBalance() * (diffMonths * ((interestRate / 100) / 12))); // 소수 첫째자리에서 올림
                // 확인용 출력
//                System.out.println("이자: " + interest);
                try {
                    account.plus(interest); // 해당 메서드 안에서 오버플로우 막아줌!
                } catch (IllegalArgumentException e) {
                    account.deactivate(); // 막아주기만 하고 비활성화는 안 시ㅋㅕ줬엇네 ㅋㅋ
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void freeAccountInterestForDate(LocalDate now, TransactionRepository tr) {
        double interestRate = 0.1;
        for (Account account : accounts.values()) {
            if (account.isActive() && !(account instanceof SavingAccount)) {
                List<Transaction> trs = tr.findTransactionByAccount(account);
                if (!trs.isEmpty()) {
                    LocalDate last = trs.get(trs.size() - 1).getDate();
                    LocalDate nowStd = LocalDate.of(now.getYear(), now.getMonth(), 1);
                    LocalDate lastStd = LocalDate.of(last.getYear(), last.getMonth(), 1);
                    long diffMonths = ChronoUnit.MONTHS.between(lastStd, nowStd);
                    long interest = (long) Math.ceil(account.getBalance() * (diffMonths * ((interestRate / 100) / 12))); // 소수 첫째자리에서 올림
                    // 확인용 출력
//                System.out.println("이자: " + interest);
                    try {
                        account.plus(interest); // 해당 메서드 안에서 오버플로우 막아줌!
                    } catch (IllegalArgumentException e) {
                        account.deactivate(); // 막아주기만 하고 비활성화는 안 시ㅋㅕ줬엇네 ㅋㅋ
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    public void closeAccount(SavingAccount savingAccount) {
        Path path = Paths.get(DEPOSIT_INFO_FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(savingAccount.getAccountNumber())) {
                    lines.set(i, line.replace("opened", "closed"));
                    break;
                }
            }
            Files.write(path, lines);
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 계좌 상태를 'closed'로 변경하는 데 실패했습니다.");
        }
    }
}
