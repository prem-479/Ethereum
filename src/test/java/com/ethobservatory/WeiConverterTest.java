package com.ethobservatory;

import com.ethobservatory.util.WeiConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeiConverterTest {

    @Test
    void convertsWeiToEth() {
        BigInteger wei = new BigInteger("1000000000000000000");
        assertEquals("1.000000000000000000", WeiConverter.weiToEth(wei).toPlainString());
    }
}
