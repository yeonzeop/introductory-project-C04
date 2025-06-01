package BanKU.enums;

public enum SavingsType {
    SHORT("단기", 0.02, 0.001),
    MID("중기", 0.03, 0.005),
    LONG("장기", 0.04, 0.001);

    private final String type;
    private final double rate;
    private final double earlyRate;

    SavingsType(String type, double rate, double earlyRate) {
        this.type = type;
        this.rate = rate;
        this.earlyRate = earlyRate;
    }

}
