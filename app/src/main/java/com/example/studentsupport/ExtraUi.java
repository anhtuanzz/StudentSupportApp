package com.example.studentsupport;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Components nhỏ, tông màu cam-trắng thống nhất với XML bản gốc. */
final class ExtraUi {
    static LinearLayout page(Activity a,String title,String subtitle){
        Ui.styleWindow(a);
        LinearLayout root=new LinearLayout(a);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfff7f8fb);
        ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);
        LinearLayout content=new LinearLayout(a);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(Ui.dp(a,18),Ui.dp(a,20),Ui.dp(a,18),Ui.dp(a,28));
        Button back=button(a,"‹  Quay lại");back.setBackgroundResource(R.drawable.button_light);back.setTextColor(Ui.INK);content.addView(back);
        back.setOnClickListener(v->a.finish());
        TextView heading=Ui.makeText(a,title,25,Ui.INK,true);heading.setPadding(0,Ui.dp(a,20),0,Ui.dp(a,6));content.addView(heading);
        TextView desc=Ui.makeText(a,subtitle,13,Ui.MUTED,false);desc.setPadding(0,0,0,Ui.dp(a,14));content.addView(desc);
        scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));a.setContentView(root);
        return content;
    }
    static Button button(Context c,String text){
        Button b=new Button(c);b.setAllCaps(false);b.setText(text);b.setTextSize(14);b.setTextColor(Color.WHITE);b.setBackgroundResource(R.drawable.button_orange);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(c,52));lp.bottomMargin=Ui.dp(c,11);b.setLayoutParams(lp);return b;
    }
    static TextView label(Context c,String title){
        TextView t=Ui.makeText(c,title,16,Ui.INK,true);t.setPadding(0,Ui.dp(c,10),0,Ui.dp(c,12));return t;
    }
    static void card(Context c,LinearLayout parent,String title,String body,View.OnClickListener action){
        LinearLayout card=Ui.cardRow(c);TextView h=Ui.makeText(c,title,16,Ui.INK,true);card.addView(h);
        TextView sub=Ui.makeText(c,body,13,Ui.MUTED,false);sub.setPadding(0,Ui.dp(c,5),0,0);card.addView(sub);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=Ui.dp(c,9);parent.addView(card,lp);
        if(action!=null){card.setClickable(true);card.setOnClickListener(action);}
    }
    static void addAction(Context c,LinearLayout parent,String name,View.OnClickListener click){Button b=button(c,name);parent.addView(b);b.setOnClickListener(click);}
}
