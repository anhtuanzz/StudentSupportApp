package com.example.studentsupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/** Người 4: xem thời hạn, gia hạn gói bằng việc tạo đơn mới. */
public final class SubscriptionsActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    private List<Models.SubscriptionItem> rows=new ArrayList<>();
    private ListView list;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_subscriptions); Ui.bindNavigation(this,"profile");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        if(!"STUDENT".equals(actor.role)){Ui.toast(this,"Chỉ sinh viên xem gói đang dùng");finish();return;}
        list=findViewById(R.id.listSubscriptions);
        list.setOnItemClickListener((p,v,index,id)->{
            if(index>=rows.size())return;
            Models.SubscriptionItem s=rows.get(index);
            Models.PackageItem pack=db.getPackage(s.packageId);
            if(pack==null || !pack.active){Ui.toast(this,"Gói hiện không còn bán để gia hạn");return;}
            new AlertDialog.Builder(this).setTitle("Gia hạn "+s.packageName)
                .setMessage("Mua thêm "+s.durationDays+" ngày với giá "+Ui.money(pack.price)+"?\nNếu chưa hết hạn, thời gian mới cộng vào ngày hết hạn cũ.")
                .setPositiveButton("Tạo đơn gia hạn",(d,which)->{
                    long orderId=db.createOrder(actor.id,s.packageId);
                    if(orderId<1){Ui.toast(this,"Không tạo được đơn gia hạn");return;}
                    Intent i=new Intent(this,PaymentActivity.class);i.putExtra("order_id",orderId);startActivity(i);
                }).setNegativeButton("Không",null).show();
        });
    }
    @Override protected void onResume(){
        super.onResume();if(db==null||actor==null||list==null)return;
        rows=db.subscriptions(actor.id);
        List<String> labels=new ArrayList<>();
        long now=System.currentTimeMillis();
        for(Models.SubscriptionItem s:rows){
            String status=s.expiresAt>now?"Đang hoạt động":"Đã hết hạn";
            long remaining=Math.max(0,(s.expiresAt-now+86_399_999)/86_400_000);
            labels.add(s.packageName+"\n"+status+" • Còn khoảng "+remaining+" ngày\nHết hạn: "+Ui.date(s.expiresAt));
        }
        if(rows.isEmpty())labels.add("Bạn chưa kích hoạt gói nào");
        list.setAdapter(Ui.listAdapter(this,labels));
    }
}
