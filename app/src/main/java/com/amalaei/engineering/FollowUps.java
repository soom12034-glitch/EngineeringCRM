package com.amalaei.engineering;
import java.time.*;
final class FollowUps {
    static String key(String date,String time){return date+"T"+time;}
    static long at(String date,String time){return LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    static boolean pending(int stage,String date,String time,String notified){return stage<4&&!date.isEmpty()&&!key(date,time).equals(notified);}
}
