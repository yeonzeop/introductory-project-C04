package BanKU.repository;


import BanKU.domain.Account;
import BanKU.domain.Member;
import BanKU.domain.SavingAccount;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        Files.lines(path)
                .map(line -> line.split("\\|"))
                .map(Member::from)
                .filter(Objects::nonNull)
                .forEach(member -> {
                    addMember(member);
                    addAccount(member.getAccounts());
                });
    }

    private void loadSavingsFile() throws IOException {
        Path path = Paths.get(DEPOSIT_INFO_FILE_PATH);
        Files.lines(path)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split("\\|"))
                .map(strings -> Map.entry(strings[0], SavingAccount.from(strings)))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isClosed())
                .forEach(entry -> {
                    try {
                        Member member = findMemberByLoginId(entry.getKey().trim());
                        member.setHasSavingAccount(true);
                        SavingAccount savingAccount = entry.getValue();
                        accounts.put(savingAccount.getAccountNumber(), savingAccount);
                        member.addAccount(savingAccount);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                });
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
        // TODO. 파일에 회원정보 저장 (추가하기)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String newLine =
                member.getLoginId().toLowerCase() + "|" +
                        member.getPassword() + "|" +
                        member.getName() + "|" +
                        member.getBirthday().format(formatter) + "|" +
                        member.getPhoneNumber() + "|";


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
        if (account == null || !account.isActive()) {
            throw new IllegalArgumentException("[WARNING] 비활성화 계좌의 계좌번호, 혹은 존재하지 않는 계좌 번호로 인하여 누락된 거래 내역이 있습니다.");
        }
        return account;
    }

    public Member findMemberByLoginId(String loginId) {
        return membersByLoginId.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(loginId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 등록되지 않은 아이디입니다."));
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
            sb.append(member.getLoginId().toLowerCase()).append("|")
                    .append(savingAccount.getAccountNumber()).append("|")
                    .append(savingAccount.getPassword()).append("|")
                    .append(savingAccount.getStartDay().format(formatter)).append("|")
                    .append(savingAccount.getEndDay().format(formatter)).append("|")
                    .append(savingAccount.isClosed() ? "closed" : "opened");
//            System.out.println("[LOG] 적금계좌 저장 형태 = " + sb.toString());
            writer.write(sb.toString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("[ERROR] 적금 계좌 정보를 파일에 저장하는 데 실패했습니다.");
            System.out.println("[ERROR MESSAGE] " + e.getMessage());
        }
    }

    public void setSavingsAccountClosed(Member member, SavingAccount account) {
        Path path = Paths.get(DEPOSIT_INFO_FILE_PATH);

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updatedLines = lines.stream()
                    .filter(line -> !line.contains(account.getAccountNumber()))
                    .toList();
            Files.write(path, updatedLines);
            saveSavingsAccount(member, account);

        } catch (IOException e) {
            System.out.println("[ERROR] 적금 계좌 정보를 파일을 변경하는 데 실패했습니다.");
        }
    }
}
