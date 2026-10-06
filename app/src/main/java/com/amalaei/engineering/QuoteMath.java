package com.amalaei.engineering;
import java.math.*;
final class QuoteMath {
 static BigDecimal number(String s){s=s.trim().replace("٬","").replace(",","").replace('٫','.');StringBuilder b=new StringBuilder();for(char c:s.toCharArray()){int n=Character.digit(c,10);b.append(n>=0?(char)('0'+n):c);}BigDecimal v=new BigDecimal(b.toString());if(v.precision()>15||Math.abs(v.scale())>6||v.signum()<0)throw new IllegalArgumentException("قيمة غير صالحة");return v;}
 static BigDecimal line(String q,String p){BigDecimal qty=number(q);if(qty.signum()==0)throw new IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر");return qty.multiply(number(p)).setScale(2,RoundingMode.HALF_UP);}
 static BigDecimal tax(BigDecimal subtotal){return subtotal.multiply(new BigDecimal("0.15")).setScale(2,RoundingMode.HALF_UP);}
 static String money(BigDecimal n){return String.format(java.util.Locale.US,"%,.2f",n);}
}
