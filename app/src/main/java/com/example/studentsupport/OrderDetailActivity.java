package com.example.studentsupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public final class OrderDetailActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    private long orderId;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_order_detail); Ui.bindNavigation(this,"orders");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        orderId=getIntent().getLongExtra("order_id",-1);
        findViewById(R.id.btnPay).setOnClickListener(v->{
            Intent i=new Intent(this,PaymentActivity.class);i.putExtra("order_id",orderId);startActivity(i);
        });
        findViewById(R.id.btnCancel).setOnClickListener(v->new AlertDialog.Builder(this)
            .setMessage("Bạn muốn hủy đơn hàng chưa thanh toán này?")
            .setPositiveButton("Hủy đơn",(dialog,which)->{
                Ui.toast(this,db.cancelOrder(actor.id,orderId)?"Đã hủy đơn hàng":"Không thể hủy đơn");display();
            }).setNegativeButton("Không",null).show());
        findViewById(R.id.btnComplete).setOnClickListener(v->{
            Ui.toast(this,db.completeOrder(actor,orderId)?"Đã đánh dấu hoàn tất":"Không thể cập nhật đơn");display();
        });
        display();
    }
    @Override protected void onResume(){super.onResume();if(db!=null&&actor!=null)display();}
    private void display(){
        Models.OrderItem o=db.getOrder(orderId);
        if(o==null || (!"ADMIN".equals(actor.role)&&o.userId!=actor.id)){
            Ui.toast(this,"Bạn không có quyền xem đơn hàng này");finish();return;
        }
        ((TextView)findViewById(R.id.txtOrder)).setText("Đơn hàng #"+o.id);
        ((TextView)findViewById(R.id.txtInfo)).setText("Sinh viên: "+o.userName+"\nGói: "+o.packageName+
            "\nTổng tiền: "+Ui.money(o.amount)+"\nTrạng thái: "+o.status+"\nNgày tạo: "+Ui.date(o.createdAt));
        findViewById(R.id.btnPay).setVisibility("STUDENT".equals(actor.role)&&"PENDING".equals(o.status)?View.VISIBLE:View.GONE);
        findViewById(R.id.btnCancel).setVisibility("STUDENT".equals(actor.role)&&"PENDING".equals(o.status)?View.VISIBLE:View.GONE);
        findViewById(R.id.btnComplete).setVisibility("ADMIN".equals(actor.role)&&"PAID".equals(o.status)?View.VISIBLE:View.GONE);
    }
}
