package com.ethobservatory.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class WeiConverter {

    public static final BigInteger WEI_PER_ETH = new BigInteger("1000000000000000000");

    private WeiConverter() {
    }

    public static BigDecimal weiToEth(BigInteger wei) {
        if (wei == null || wei.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wei).divide(new BigDecimal(WEI_PER_ETH), 18, RoundingMode.HALF_UP);
    }

    public static BigDecimal weiToEth(String wei) {
        return weiToEth(new BigInteger(wei));
    }
}
