package com.amalaei.engineering;

import android.app.*;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import java.math.BigDecimal;

public final class CalculatorActivity extends Activity {
    private final Calculator calculator=new Calculator();
    private TextView expression,result,status,subtitle,screenCaption,outputCaption,screenTaxLine;
    private Button addMode,includedMode,rateButton;
    private Typeface arabic,numbers;
    private LinearLayout body;
    private BigDecimal taxPercent=new BigDecimal("15");
    private boolean includedTax=false;
    private final int INK=0xff102a43,ACCENT=0xff0f766e,MUTED=0xff8790a3,BG=0xfff5f7fa;

    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private GradientDrawable shape(int color,int radius){GradientDrawable drawable=new GradientDrawable();drawable.setColor(color);drawable.setCornerRadius(dp(radius));return drawable;}

    private TextView text(String value,int size,int color,boolean numeric){
        TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(color);view.setTypeface(numeric?numbers:arabic);view.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);view.setTextDirection(numeric?View.TEXT_DIRECTION_LTR:View.TEXT_DIRECTION_RTL);
        if(numeric){view.setSingleLine(true);view.setFontFeatureSettings("tnum");}return view;
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);arabic=Typeface.createFromAsset(getAssets(),"fonts/Cairo.ttf");numbers=Typeface.create("sans-serif-medium",Typeface.NORMAL);
        String savedRate=state==null?getPreferences(0).getString("taxPercent","15"):state.getString("taxPercent","15");try{taxPercent=Calculator.taxRate(savedRate);}catch(Exception ignored){taxPercent=new BigDecimal("15");}
        includedTax=state!=null&&state.getBoolean("includedTax",false);if(state!=null)calculator.restore(state.getString("expression",""),state.getBoolean("finished",false));
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(true);scroll.setScrollbarFadingEnabled(false);scroll.setSmoothScrollingEnabled(true);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setBackgroundColor(BG);scroll.addView(body,new ScrollView.LayoutParams(-1,-2));setContentView(scroll);
        body.setOnApplyWindowInsetsListener((view,insets)->{int top,bottom;if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}view.setPadding(dp(18),top+dp(12),dp(18),bottom+dp(96));return insets;});

        LinearLayout header=new LinearLayout(this);header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);header.setGravity(Gravity.CENTER_VERTICAL);LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);titles.addView(text("حاسبة الأسعار",23,INK,false));subtitle=text("",12,MUTED,false);titles.addView(subtitle);header.addView(titles,new LinearLayout.LayoutParams(0,-2,1));Button back=key("رجوع","backscreen");header.addView(back,new LinearLayout.LayoutParams(dp(68),dp(44)));back.setOnClickListener(view->finish());LinearLayout.LayoutParams headerParams=new LinearLayout.LayoutParams(-1,-2);headerParams.setMargins(0,0,0,dp(16));body.addView(header,headerParams);

        LinearLayout screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);screen.setPadding(dp(20),dp(14),dp(20),dp(14));screen.setBackground(shape(INK,24));screen.setElevation(dp(3));body.addView(screen,new LinearLayout.LayoutParams(-1,-2));
        screenCaption=text("المبلغ المدخل / العملية  ·  SAR",11,0xffccfbf1,false);screen.addView(screenCaption);
        expression=text("0",18,0xff99f6e4,true);expression.setHorizontalScrollBarEnabled(false);expression.setAutoSizeTextTypeUniformWithConfiguration(13,18,1,android.util.TypedValue.COMPLEX_UNIT_SP);screen.addView(expression,new LinearLayout.LayoutParams(-1,dp(30)));
        View screenDivider=new View(this);screenDivider.setBackgroundColor(0x335eead4);LinearLayout.LayoutParams screenDividerParams=new LinearLayout.LayoutParams(-1,dp(1));screenDividerParams.setMargins(0,dp(3),0,dp(5));screen.addView(screenDivider,screenDividerParams);
        outputCaption=text("",11,0xffccfbf1,false);screen.addView(outputCaption);
        result=text("0.00",38,Color.WHITE,true);result.setAutoSizeTextTypeUniformWithConfiguration(21,38,1,android.util.TypedValue.COMPLEX_UNIT_SP);result.setTextIsSelectable(true);screen.addView(result,new LinearLayout.LayoutParams(-1,dp(54)));
        screenTaxLine=text("",11,0xff99f6e4,false);screenTaxLine.setSingleLine(true);screenTaxLine.setAutoSizeTextTypeUniformWithConfiguration(9,11,1,android.util.TypedValue.COMPLEX_UNIT_SP);screen.addView(screenTaxLine,new LinearLayout.LayoutParams(-1,dp(24)));

        keypadRow(new String[]{"7","8","9","÷"},new String[]{"7","8","9","/"});
        keypadRow(new String[]{"4","5","6","×"},new String[]{"4","5","6","*"});
        keypadRow(new String[]{"1","2","3","−"},new String[]{"1","2","3","-"});
        keypadRow(new String[]{"0",".","=","+"},new String[]{"0",".","=","+"});
        keypadRow(new String[]{"AC","⌫","±"},new String[]{"C","back","sign"});

        TextView modeHeading=text("إعدادات الضريبة",12,MUTED,false);modeHeading.setPadding(0,dp(14),0,dp(5));body.addView(modeHeading);
        LinearLayout modes=new LinearLayout(this);modes.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);body.addView(modes,new LinearLayout.LayoutParams(-1,dp(58)));
        addMode=modeButton("إضافة الضريبة");includedMode=modeButton("المبلغ شامل الضريبة");LinearLayout.LayoutParams modeParams=new LinearLayout.LayoutParams(0,-1,1);modeParams.setMargins(dp(3),0,dp(3),0);modes.addView(addMode,modeParams);modes.addView(includedMode,modeParams);addMode.setOnClickListener(view->{includedTax=false;display();});includedMode.setOnClickListener(view->{includedTax=true;display();});
        rateButton=key("","tax");LinearLayout.LayoutParams rateParams=new LinearLayout.LayoutParams(-1,dp(50));rateParams.setMargins(dp(3),dp(8),dp(3),dp(8));body.addView(rateButton,rateParams);rateButton.setOnClickListener(view->chooseRate());

        status=text("",12,0xff8992a4,false);status.setMinHeight(dp(27));body.addView(status);
        TextView note=text("في وضع «المبلغ شامل الضريبة» يستخرج التطبيق الصافي والضريبة الصحيحة من الإجمالي؛ مثال 1000 شامل 15% = صافي 869.57 وضريبة 130.43.",11,MUTED,false);note.setSingleLine(false);note.setPadding(0,dp(10),0,dp(18));body.addView(note);display();
    }

    private Button modeButton(String label){Button button=key(label,"tax");button.setTextSize(13);return button;}

    private void styleMode(Button button,boolean selected){button.setTextColor(selected?Color.WHITE:ACCENT);button.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x220f766e),shape(selected?ACCENT:0xffdff7f3,16),null));}

    private Button key(String label,String kind){
        boolean digit=kind.matches("[0-9.]"),primary=kind.equals("="),tax=kind.equals("tax");int color=tax?0xffdff7f3:primary?ACCENT:digit?Color.WHITE:0xffe8f1f3;Button button=new Button(this);button.setText(label);button.setTextSize(tax?15:kind.equals("backscreen")?13:kind.equals("C")?20:29);button.setTypeface(tax||kind.equals("backscreen")?arabic:numbers);button.setTextColor(primary?Color.WHITE:tax?ACCENT:digit?INK:ACCENT);button.setAllCaps(false);button.setMinHeight(0);button.setMinimumHeight(0);button.setPadding(dp(4),0,dp(4),0);button.setStateListAnimator(null);button.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x220f766e),shape(color,18),null));button.setContentDescription(kind.equals("back")?"حذف آخر رقم":kind.equals("C")?"مسح العملية":label);return button;
    }

    private void keypadRow(String[] labels,String[] keys){LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(-1,dp(58));rowParams.setMargins(0,dp(3),0,dp(3));body.addView(row,rowParams);for(int i=0;i<labels.length;i++){String kind=keys[i];Button button=key(labels[i],kind);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-1,1);params.setMargins(dp(3),0,dp(3),0);row.addView(button,params);button.setOnClickListener(view->act(kind));}}

    private String percent(){return Calculator.plain(taxPercent)+"%";}

    private void chooseRate(){
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),dp(8),dp(20),0);TextView hint=text("أدخل النسبة من 0 إلى 100، ويمكن استخدام نسبة عشرية مثل 14.5",13,INK,false);hint.setSingleLine(false);form.addView(hint);EditText input=new EditText(this);input.setTypeface(numbers);input.setText(Calculator.plain(taxPercent));input.setSelectAllOnFocus(true);input.setTextDirection(View.TEXT_DIRECTION_LTR);input.setGravity(Gravity.START);input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);form.addView(input,new LinearLayout.LayoutParams(-1,dp(55)));
        new AlertDialog.Builder(this).setTitle("نسبة الضريبة").setView(form).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(dialog,which)->{try{taxPercent=Calculator.taxRate(input.getText().toString());getPreferences(0).edit().putString("taxPercent",Calculator.plain(taxPercent)).apply();display();}catch(IllegalArgumentException invalid){status.setTextColor(0xffbd4a57);status.setText(invalid.getMessage());}}).show();
    }

    private void act(String key){try{calculator.press(key);display();}catch(IllegalArgumentException error){status.setTextColor(0xffbd4a57);status.setText(error.getMessage());}}

    private void display(){
        String rate=percent();subtitle.setText((includedTax?"استخراج الضريبة من الإجمالي":"إضافة الضريبة إلى الصافي")+" • "+rate);screenCaption.setText("المبلغ المدخل / العملية  ·  SAR");outputCaption.setText(includedTax?"الصافي بعد استخراج الضريبة":"الإجمالي شامل الضريبة");rateButton.setText("تغيير نسبة الضريبة  •  "+rate);styleMode(addMode,!includedTax);styleMode(includedMode,includedTax);
        String entered=calculator.expression();expression.setText(entered.isEmpty()?"0":entered.replace('*','×').replace('/','÷'));
        try{
            BigDecimal value;boolean pending=false;try{value=calculator.value();}catch(IllegalArgumentException incomplete){if(!"أكمل العملية أولًا".equals(incomplete.getMessage()))throw incomplete;String prefix=entered;while(!prefix.isEmpty()&&"+*/-".indexOf(prefix.charAt(prefix.length()-1))>=0)prefix=prefix.substring(0,prefix.length()-1);value=Calculator.evaluate(prefix.isEmpty()?"0":prefix);pending=true;}
            BigDecimal[] values=Calculator.vat(value,taxPercent,includedTax);BigDecimal output=includedTax?values[0]:values[2];result.setText(Calculator.money(output));screenTaxLine.setText("قيمة الضريبة "+rate+"  •  "+Calculator.money(values[1])+" SAR");status.setTextColor(0xff8992a4);status.setText(pending?"أدخل العدد التالي • النتيجة لآخر تعبير مكتمل":includedTax?"الصافي بعد خصم الضريبة من الإجمالي":"الإجمالي بعد إضافة الضريبة");
        }catch(IllegalArgumentException error){result.setText("—");screenTaxLine.setText("تعذر حساب الضريبة");status.setTextColor(0xffbd4a57);status.setText(error.getMessage());}
    }

    @Override public void onPostCreate(Bundle state){super.onPostCreate(state);AppLanguage.apply(this);AppLanguage.watch(this);}
    @Override protected void onSaveInstanceState(Bundle state){state.putString("expression",calculator.expression());state.putBoolean("finished",calculator.finished());state.putString("taxPercent",Calculator.plain(taxPercent));state.putBoolean("includedTax",includedTax);super.onSaveInstanceState(state);}
}
