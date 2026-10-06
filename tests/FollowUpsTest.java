package com.amalaei.engineering;
public final class FollowUpsTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args){
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Riyadh"));
        check(FollowUps.at("2026-10-05","09:00")==java.time.Instant.parse("2026-10-05T06:00:00Z").toEpochMilli());
        check(FollowUps.pending(0,"2026-10-05","09:00",""));
        check(!FollowUps.pending(4,"2026-10-05","09:00",""));
        check(!FollowUps.pending(5,"2026-10-05","09:00",""));
        check(!FollowUps.pending(0,"","09:00",""));
        check(!FollowUps.pending(0,"2026-10-05","09:00","2026-10-05T09:00"));
        check(FollowUps.pending(0,"2026-10-06","09:00","2026-10-05T09:00"));
        check(FollowUps.pending(0,"2026-10-05","10:00","2026-10-05T09:00"));
        System.out.println("PASS: Riyadh time, completed/lost exclusion, empty-date exclusion, one notice per date/time and rescheduling.");
    }
}
