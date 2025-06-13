package BanKU.enums;

public enum SavingsType {
    SHORT("단기", 0.02, 0.001, 6),
    MID("중기", 0.03, 0.005, 12),
    LONG("장기", 0.04, 0.001, 18);

    private final String type;
    private final double rate;
    private final double earlyRate;
    private final int month;

    SavingsType(String type, double rate, double earlyRate, int month) {
        this.type = type;
        this.rate = rate;
        this.earlyRate = earlyRate;
        this.month = month;
    }

    public double getRate() {
        return rate;
    }

    public double getEarlyRate() {
        return earlyRate;
    }

    public int getMonth() {
        return month;
    }
}
