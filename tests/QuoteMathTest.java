package com.amalaei.engineering;
import java.math.*;
public class QuoteMathTest {
 static void eq(String expected,BigDecimal actual){if(!expected.equals(actual.toPlainString()))throw new AssertionError(expected+" != "+actual);}
 public static void main(String[] args){BigDecimal sub=QuoteMath.line("1","16086.96");eq("16086.96",sub);eq("2413.04",QuoteMath.tax(sub));eq("18500.00",sub.add(QuoteMath.tax(sub)));eq("30.00",QuoteMath.line("٢٫٥","١٢"));BigDecimal multi=QuoteMath.line("2","100.005").add(QuoteMath.line("3","20"));eq("260.01",multi);eq("39.00",QuoteMath.tax(multi));for(String bad:new String[]{"-1","NaN","1e100","0"}){try{QuoteMath.line(bad,"1");throw new AssertionError(bad);}catch(IllegalArgumentException ok){}}System.out.println("Quotation arithmetic passed");}
}
