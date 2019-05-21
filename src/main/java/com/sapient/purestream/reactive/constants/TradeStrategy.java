package com.sapient.purestream.reactive.constants;

public enum TradeStrategy {
    FIVE_FIFTEEN_PCT_POV("5-15%"),
    TEN_THIRTY_PCT_POV("10-30%"),
    LS("Liquidity Seeking"),
    MACH2("Mach 2"),
    MACH4("Mach 4");

    private final String value;

    TradeStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
