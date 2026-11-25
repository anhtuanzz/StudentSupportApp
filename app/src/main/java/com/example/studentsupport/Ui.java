package com.example.studentsupport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Bộ giao diện cam-trắng: navigation, danh sách dạng card và các tiện ích dùng chung. */
final class Ui {
    static final int ORANGE = Color.rgb(255, 120, 0);
    static final int INK = Color.rgb(32, 32, 45);
    static final int MUTED = Color.rgb(116, 119, 133);
    static final int WHITE = Color.WHITE;
    static String money(long v) { return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + " đ"; }
    static String date(long millis) { return new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN")).format(new Date(millis)); }
    static String value(EditText edit) { return edit.getText().toString().trim(); }
    static void toast(Context c, String message) { Toast.makeText(c, message, Toast.LENGTH_LONG).show(); }
    static int dp(Context c, int value) { return Math.round(c.getResources().getDisplayMetrics().density * value); }

    static EditText field(Context c, String hint, String initial, int type) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setText(initial == null ? "" : initial);
        e.setInputType(type);
        e.setSingleLine((type & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        e.setPadding(dp(c,15),dp(c,12),dp(c,15),dp(c,12));
        e.setTextColor(INK);
        e.setTextSize(14f);
        e.setBackgroundResource(R.drawable.input_bg);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.bottomMargin=dp(c,10);
        e.setLayoutParams(params);
        return e;
    }
    static LinearLayout form(Context c) {
        LinearLayout layout = new LinearLayout(c);
        layout.setPadding(dp(c,20),dp(c,14),dp(c,20),dp(c,8));
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    /** Cài bottom navigation 5 tab, không cần Jetpack Navigation. */
    static void bindNavigation(Activity a, String active) {
        styleWindow(a);
        int[] containerIds={R.id.navHome,R.id.navPackages,R.id.navOrders,R.id.navSupport,R.id.navProfile};
        int[] iconIds={R.id.navHomeIcon,R.id.navPackagesIcon,R.id.navOrdersIcon,R.id.navSupportIcon,R.id.navProfileIcon};
        int[] labelIds={R.id.navHomeLabel,R.id.navPackagesLabel,R.id.navOrdersLabel,R.id.navSupportLabel,R.id.navProfileLabel};
        String[] names={"home","packages","orders","support","profile"};
        @SuppressWarnings("unchecked") Class<? extends Activity>[] pages=new Class[]{HomeActivity.class,PackagesActivity.class,OrdersActivity.class,SupportActivity.class,ProfileActivity.class};
        for(int i=0;i<names.length;i++) {
            View item=a.findViewById(containerIds[i]);
            if(item==null)continue;
            final Class<? extends Activity> target=pages[i];
            if(a.getClass()!=target) item.setOnClickListener(v->{
                Intent go=new Intent(a,target);
                go.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                a.startActivity(go);
            });
            TextView label=a.findViewById(labelIds[i]);
            ImageView icon=a.findViewById(iconIds[i]);
            int color=names[i].equals(active)?ORANGE:MUTED;
            if(label!=null){label.setTextColor(color);label.setTypeface(Typeface.DEFAULT,names[i].equals(active)?Typeface.BOLD:Typeface.NORMAL);}
            if(icon!=null)icon.setColorFilter(color);
        }
        View back=a.findViewById(R.id.btnBack);
        if(back!=null)back.setOnClickListener(v->a.finish());
    }
    static void styleWindow(Activity a) {
        a.getWindow().setStatusBarColor(Color.WHITE);
        a.getWindow().setNavigationBarColor(Color.WHITE);
        a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }
    static GradientDrawable cardBg(Context c) {
        GradientDrawable b=new GradientDrawable();
        b.setColor(Color.WHITE);b.setCornerRadius(dp(c,18));
        b.setStroke(dp(c,1), Color.rgb(237,240,244));
        return b;
    }
    static TextView makeText(Context c, String value, int size, int color, boolean bold) {
        TextView text=new TextView(c);
        text.setText(value);text.setTextSize(size);text.setTextColor(color);
        if(bold)text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return text;
    }
    static LinearLayout cardRow(Context c) {
        LinearLayout root=new LinearLayout(c);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(c,16),dp(c,15),dp(c,16),dp(c,15));
        root.setBackground(cardBg(c));
        root.setMinimumHeight(dp(c,90));
        return root;
    }
    static ArrayAdapter<String> listAdapter(Context c, List<String> labels) {
        return new ArrayAdapter<String>(c,android.R.layout.simple_list_item_1,labels) {
            @Override public View getView(int pos, View old, ViewGroup parent) {
                LinearLayout root=cardRow(c);
                String[] lines=getItem(pos).split("\n",2);
                root.addView(makeText(c,lines[0],16,INK,true));
                if(lines.length>1){
                    TextView info=makeText(c,lines[1],13,MUTED,false);
                    info.setLineSpacing(dp(c,3),1f);
                    LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.topMargin=dp(c,7);
                    root.addView(info,ip);
                }
                TextView arrow=makeText(c,"Xem chi tiết   ›",12,ORANGE,true);
                LinearLayout.LayoutParams arrowLp=new LinearLayout.LayoutParams(-2,-2);
                arrowLp.gravity=Gravity.END;arrowLp.topMargin=dp(c,10);
                root.addView(arrow,arrowLp);
                return root;
            }
        };
    }
    /** Adapter sử dụng model gói thật và giữ nguyên thứ tự danh sách để click đúng ID. */
    static ArrayAdapter<Models.PackageItem> packageAdapter(Context c, List<Models.PackageItem> items) {
        return new ArrayAdapter<Models.PackageItem>(c,android.R.layout.simple_list_item_1,items) {
            @Override public View getView(int pos,View old,ViewGroup parent) {
                Models.PackageItem p=getItem(pos);
                LinearLayout outer=cardRow(c);
                LinearLayout r=new LinearLayout(c);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
                TextView graphic=makeText(c,packageIcon(p.category),29,ORANGE,true);
                graphic.setGravity(Gravity.CENTER);
                graphic.setBackgroundResource(R.drawable.chip_bg);
                LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(dp(c,62),dp(c,66));gp.rightMargin=dp(c,13);
                r.addView(graphic,gp);
                LinearLayout info=new LinearLayout(c);info.setOrientation(LinearLayout.VERTICAL);
                info.addView(makeText(c,p.name,16,INK,true));
                TextView desc=makeText(c,p.description,12,MUTED,false);
                desc.setMaxLines(2);desc.setEllipsize(android.text.TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2);dp.topMargin=Ui.dp(c,5);info.addView(desc,dp);
                String displayPrice=p.salePrice>0&&p.salePrice<p.price
                    ? money(p.salePrice)+"  (gốc "+money(p.price)+")" : money(p.price);
                TextView price=makeText(c,displayPrice,16,ORANGE,true);
                LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);mp.topMargin=Ui.dp(c,7);info.addView(price,mp);
                TextView days=makeText(c,"◷  " + p.durationDays + " ngày   •   " + p.lessonCount + " bài học   •   " + p.category,12,MUTED,false);
                LinearLayout.LayoutParams durp=new LinearLayout.LayoutParams(-1,-2);durp.topMargin=Ui.dp(c,4);info.addView(days,durp);
                r.addView(info,new LinearLayout.LayoutParams(0,-2,1));
                outer.addView(r);
                TextView link=makeText(c,"Xem chi tiết gói   ›",12,ORANGE,true);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
                lp.gravity=Gravity.END;lp.topMargin=Ui.dp(c,10);outer.addView(link,lp);
                return outer;
            }
        };
    }
    private static String packageIcon(String category) {
        String v=category==null?"":category.toLowerCase(Locale.ROOT);
        if(v.contains("ngoại ngữ")||v.contains("anh"))return "🌍";
        if(v.contains("tin học")||v.contains("cntt"))return "💻";
        if(v.contains("kỹ năng"))return "🎯";
        return "📚";
    }
}
