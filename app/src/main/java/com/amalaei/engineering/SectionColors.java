package com.amalaei.engineering;

final class SectionColors {
    private SectionColors() {
    }

    static int dark(String str) {
        str.hashCode();
        switch (str) {
            case "calculator":
                return -7118309;
            case "ledger":
                return -15243696;
            case "quotes":
                return -14588050;
            case "messages":
                return -14066312;
            case "ads":
                return -5681362;
            case "today":
                return -10337382;
            case "clients":
                return -15444342;
            case "reports":
                return -12562536;
            case "settings":
                return -12494995;
            default:
                return -15712682;
        }
    }

    static int light(String str) {
        str.hashCode();
        switch (str) {
            case "calculator":
                return -2642603;
            case "ledger":
                return -11553637;
            case "quotes":
                return -10046028;
            case "messages":
                return -9721148;
            case "ads":
                return -1531277;
            case "today":
                return -6059042;
            case "clients":
                return -11097629;
            case "reports":
                return -7627555;
            case "settings":
                return -7232071;
            default:
                return -11752526;
        }
    }

    static int tint(String str) {
        str.hashCode();
        switch (str) {
            case "calculator":
                return -265252;
            case "ledger":
                return -1706513;
            case "quotes":
                return -1575693;
            case "messages":
                return -1510409;
            case "ads":
                return -200987;
            case "today":
                return -988422;
            case "clients":
                return -1575937;
            case "reports":
                return -1315333;
            case "settings":
                return -1183243;
            default:
                return -1576205;
        }
    }
}
