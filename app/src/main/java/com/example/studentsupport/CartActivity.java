package com.example.studentsupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.util.List;

/** Giỏ & yêu thích; checkout mỗi gói sinh một đơn chờ thanh toán. */
public final class CartActivity extends Activity {
    private DbHelper db;private ExtraStore store;private Models.User user;private LinearLayout content;
    @Override protected void onCreate(Bundle s){super.onCreate(s);db=new DbHelper(this);user=Session.require(this,db);if(user==null)return;store=new ExtraStore(db);build();}
    private void build(){
        content=ExtraUi.page(this,"🛒 Giỏ hàng & Yêu thích","Ưu đãi sinh viên .edu.vn 10%; áp dụng mã giảm giá cao hơn khi có. Thanh toán ở từng đơn trong mục Đơn hàng.");
        content.addView(ExtraUi.label(this,"Giỏ hàng của bạn"));
        List<Long> cart=store.ids("cart_items",user.id);long total=0;
        for(long id:cart){Models.PackageItem p=db.getPackage(id);if(p==null)continue;
            int cost=store.price(user,p,0);total+=cost;
            ExtraUi.card(this,content,p.name,Ui.money(cost)+" · "+p.durationDays+" ngày\nChạm để xóa khỏi giỏ",v->{
                new AlertDialog.Builder(this).setMessage("Xóa "+p.name+" khỏi giỏ?").setPositiveButton("Xóa",(d,w)->{store.removeCart(user.id,id);build();}).setNegativeButton("Hủy",null).show();
            });
        }
        if(cart.isEmpty())ExtraUi.card(this,content,"Giỏ hàng trống","Chọn một gói rồi nhấn Thêm vào giỏ",null);
        content.addView(ExtraUi.label(this,"Tạm tính: "+Ui.money(total)));
        EditText voucher=Ui.field(this,"Mã giảm giá (VD: EXAM20)","",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);content.addView(voucher);
        ExtraUi.addAction(this,content,"Tạo đơn cho các gói trong giỏ",v->{
            try{int count=store.checkout(user,Ui.value(voucher));
                if(count==-1){Ui.toast(this,"Mã không tồn tại hoặc đã hết hạn");return;}
                if(count==0){Ui.toast(this,"Chưa có gói hợp lệ trong giỏ");return;}
                new AlertDialog.Builder(this).setTitle("Đã tạo "+count+" đơn hàng").setMessage("Mỗi gói được tạo một đơn riêng. Mở Đơn hàng để thanh toán QR mô phỏng từng đơn.")
                    .setPositiveButton("Xem đơn hàng",(d,w)->startActivity(new Intent(this,OrdersActivity.class))).setNegativeButton("Đóng",(d,w)->build()).show();
            }catch(Exception e){Ui.toast(this,"Không thể tạo đơn: "+e.getMessage());}
        });
        content.addView(ExtraUi.label(this,"Danh sách yêu thích"));
        List<Long> fav=store.ids("favorites",user.id);
        for(long id:fav){Models.PackageItem p=db.getPackage(id);if(p==null)continue;
            ExtraUi.card(this,content,"♡ "+p.name,Ui.money(p.price)+" · Nhấn để xem chi tiết",v->{Intent i=new Intent(this,PackageDetailActivity.class);i.putExtra("package_id",id);startActivity(i);});
        }
        if(fav.isEmpty())ExtraUi.card(this,content,"Chưa có yêu thích","Nhấn ♡ trong trang chi tiết gói để lưu lại",null);
    }
    @Override protected void onResume(){super.onResume();if(store!=null)build();}
}
