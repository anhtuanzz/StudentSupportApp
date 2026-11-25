package com.example.studentsupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/** Người 3: lịch sử đơn hàng (admin thấy tất cả, sinh viên thấy đơn của mình). */
public final class OrdersActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    private List<Models.OrderItem> rows = new ArrayList<>();
    private ListView list;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_orders); Ui.bindNavigation(this,"orders");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        list=findViewById(R.id.listOrders);
        list.setOnItemClickListener((p,v,index,id)->{
            if(index>=rows.size())return;
            Intent intent=new Intent(this,OrderDetailActivity.class);
            intent.putExtra("order_id",rows.get(index).id);startActivity(intent);
        });
    }
    @Override protected void onResume(){
        super.onResume();if(db==null||actor==null)return;
        rows=db.orders(actor);
        List<String> labels=new ArrayList<>();
        for(Models.OrderItem o:rows){
            String user="ADMIN".equals(actor.role)?" • "+o.userName:"";
            labels.add("#"+o.id+" • "+o.packageName+user+"\n"+Ui.money(o.amount)+"  |  "+o.status+"\n"+Ui.date(o.createdAt));
        }
        if(rows.isEmpty())labels.add("Chưa có đơn hàng nào");
        list.setAdapter(Ui.listAdapter(this,labels));
    }
}
