package com.amalaei.engineering;

import java.math.BigDecimal;

public final class TaxModesTest {
    private static void eq(String expected,BigDecimal actual){if(new BigDecimal(expected).compareTo(actual)!=0)throw new AssertionError(expected+" != "+actual);}
    private static void values(BigDecimal[] actual,String base,String tax,String total){eq(base,actual[0]);eq(tax,actual[1]);eq(total,actual[2]);}

    public static void main(String[] args){
        values(Calculator.vat(new BigDecimal("1000"),new BigDecimal("15"),false),"1000.00","150.00","1150.00");
        values(Calculator.vat(new BigDecimal("1000"),new BigDecimal("15"),true),"869.57","130.43","1000.00");
        values(Calculator.vat(new BigDecimal("1000"),new BigDecimal("5"),true),"952.38","47.62","1000.00");
        values(Calculator.vat(new BigDecimal("1000"),new BigDecimal("14.5"),false),"1000.00","145.00","1145.00");
        eq("15.5",Calculator.taxRate("١٥٫٥"));
        for(String invalid:new String[]{"-1","100.1","abc"})try{Calculator.taxRate(invalid);throw new AssertionError("accepted rate "+invalid);}catch(IllegalArgumentException expected){}
        System.out.println("PASS: configurable exclusive and tax-inclusive calculations.");
    }
}
