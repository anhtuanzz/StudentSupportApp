package com.example.studentsupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

/** Trang chủ mới — các card hiển thị số liệu thực từ SQLite. */
public final class HomeActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_home);
        Ui.bindNavigation(this,"home");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        findViewById(R.id.btnPackages).setOnClickListener(v->open(PackagesActivity.class));
        findViewById(R.id.btnOrders).setOnClickListener(v->open(OrdersActivity.class));
        findViewById(R.id.btnSubscriptions).setOnClickListener(v->open(SubscriptionsActivity.class));
        findViewById(R.id.btnSupport).setOnClickListener(v->open(SupportActivity.class));
        findViewById(R.id.btnProfile).setOnClickListener(v->open(ProfileActivity.class));
        if("ADMIN".equals(actor.role)) {
            findViewById(R.id.btnStats).setVisibility(View.VISIBLE);
            findViewById(R.id.btnStats).setOnClickListener(v->open(StatsActivity.class));
            findViewById(R.id.btnSubscriptions).setVisibility(View.GONE);
        }
        LinearLayout parent=(LinearLayout)findViewById(R.id.btnLogout).getParent();
        if("STUDENT".equals(actor.role)){
            addHomeAction(parent,"🛒 Giỏ hàng & Yêu thích",CartActivity.class);
            addHomeAction(parent,"📚 Không gian học tập / Quiz",LearningActivity.class);
            addHomeAction(parent,"🔔 Thông báo học tập",NotificationsActivity.class);
        }else{
            addHomeAction(parent,"🛠 Quản trị: sinh viên, khuyến mãi, học liệu",AdminToolsActivity.class);
        }
        findViewById(R.id.cardChatbot).setOnClickListener(v->open(ChatbotActivity.class));
        findViewById(R.id.btnLogout).setOnClickListener(v->Session.logout(this));
    }
    private void addHomeAction(LinearLayout parent,String title,Class<? extends Activity> destination){
        Button b=ExtraUi.button(this,title);
        parent.addView(b,parent.indexOfChild(findViewById(R.id.btnLogout)));
        b.setOnClickListener(v->open(destination));
    }
    private void open(Class<? extends Activity> type){startActivity(new Intent(this,type));}
    @Override protected void onResume(){super.onResume();if(db!=null&&actor!=null)refresh();}
    private void refresh(){
        Models.User current=db.getUser(actor.id);
        if(current==null)return;
        ((TextView)findViewById(R.id.txtWelcome)).setText("Chào mừng trở lại,\n"+current.name+"!");
        ((TextView)findViewById(R.id.txtRole)).setText("Vai trò: "+("ADMIN".equals(current.role)?"Quản trị viên":"Sinh viên"));
        List<Models.OrderItem> orders=db.orders(current);
        List<Models.TicketItem> tickets=db.tickets(current);
        List<Models.SubscriptionItem> subs="STUDENT".equals(current.role)?db.subscriptions(current.id):new java.util.ArrayList<>();
        long active=0,now=System.currentTimeMillis();
        for(Models.SubscriptionItem s:subs)if(s.expiresAt>now)active++;
        ((TextView)findViewById(R.id.txtActiveCount)).setText(String.valueOf(active));
        ((TextView)findViewById(R.id.txtOrderCount)).setText(String.valueOf(orders.size()));
        ((TextView)findViewById(R.id.txtTicketCount)).setText(String.valueOf(tickets.size()));
        List<Models.PackageItem> featured=db.packages("","Tất cả danh mục");
        if(!featured.isEmpty()){
            Models.PackageItem p=featured.get(0);
            for (Models.PackageItem item : featured) {
                if (item.name.contains("Luyện thi toàn diện")) {
                    p = item;
                    break;
                }
            }
            final Models.PackageItem selected = p;
            ((TextView)findViewById(R.id.txtFeaturedName)).setText(p.name);
            ((TextView)findViewById(R.id.txtFeaturedPrice)).setText(Ui.money(p.price)+"  •  "+p.durationDays+" ngày");
            findViewById(R.id.cardFeatured).setOnClickListener(v->{
                Intent i=new Intent(this,PackageDetailActivity.class);
                i.putExtra("package_id",selected.id);startActivity(i);
            });
        }
    }
}
