package com.amalaei.engineering;

final class CustomerActivity {
    static final String[] KEYS={"","services","trade","technical_support","maintenance","marketing","direct_sales","retail","wholesale","logistics","digital_marketing","craft"};
    static final String[] LABELS={"غير محدد","خدمات","تجارة","دعم فني","صيانة","تسويق","بيع مباشر","بيع تجزئة","بيع جملة","خدمات لوجستية","تسويق رقمي","نشاط حرفي"};

    private CustomerActivity() {}

    static boolean valid(String key){for(String item:KEYS)if(item.equals(key))return true;return false;}
    static int index(String key){for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(key))return i;return 0;}
    static String label(String key){return LABELS[index(key)];}
}
