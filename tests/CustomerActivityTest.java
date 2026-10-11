package com.amalaei.engineering;

public class CustomerActivityTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args){
        check(CustomerActivity.KEYS.length==CustomerActivity.LABELS.length);
        check(CustomerActivity.KEYS.length==12);
        for(int i=0;i<CustomerActivity.KEYS.length;i++){check(CustomerActivity.valid(CustomerActivity.KEYS[i]));check(CustomerActivity.index(CustomerActivity.KEYS[i])==i);check(CustomerActivity.label(CustomerActivity.KEYS[i]).equals(CustomerActivity.LABELS[i]));}
        check(!CustomerActivity.valid("invalid"));
        check(CustomerActivity.label("invalid").equals("غير محدد"));
        System.out.println("PASS: customer business categories are valid and single-choice.");
    }
}
