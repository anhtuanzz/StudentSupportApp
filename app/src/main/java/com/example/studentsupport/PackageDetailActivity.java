package com.example.studentsupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.AlertDialog;
import android.text.InputType;
import android.net.Uri;

public final class PackageDetailActivity extends Activity {
    private TextView extraInfoText;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_package_detail); Ui.bindNavigation(this,"packages");
        DbHelper db=new DbHelper(this);Models.User actor=Session.require(this,db);if(actor==null)return;
        long id=getIntent().getLongExtra("package_id",-1);
        Models.PackageItem p=db.getPackage(id);
        if(p==null){Ui.toast(this,"Không tìm thấy gói");finish();return;}
        ((TextView)findViewById(R.id.txtName)).setText(p.name);
        String priceText=p.salePrice>0 && p.salePrice<p.price
            ? "Giá gốc: "+Ui.money(p.price)+"\nGiá ưu đãi: "+Ui.money(p.salePrice)
            : "Giá: "+Ui.money(p.price);
        ((TextView)findViewById(R.id.txtInfo)).setText("Mã gói: "+p.packageCode+"\nDanh mục: "+p.category+"\n"+priceText+
            "\nThời hạn: "+p.durationDays+" ngày • "+p.lessonCount+" bài học");
        ((TextView)findViewById(R.id.txtDescription)).setText("Mô tả:\n"+p.description);
        TextView benefitText=findViewById(R.id.txtBenefits);
        if(benefitText!=null)benefitText.setText(p.benefits.isEmpty()?"Quyền lợi đang được cập nhật":p.benefits.replace("\n","\n✓  ").replaceFirst("^","✓  "));
        TextView supportText=findViewById(R.id.txtSupportChannel);
        if(supportText!=null)supportText.setText("Hình thức hỗ trợ: "+p.supportChannel);
        ExtraStore extra=new ExtraStore(db);
        LinearLayout parent=(LinearLayout)findViewById(R.id.btnBuy).getParent();
        TextView meta=Ui.makeText(this,extra.details(id)+"\n"+extra.packageRating(id),14,Ui.MUTED,false);
        extraInfoText=meta;
        parent.addView(meta,parent.indexOfChild(findViewById(R.id.btnBuy)));
        if("STUDENT".equals(actor.role)){
            Button cart=ExtraUi.button(this,"🛒 Thêm vào giỏ hàng");parent.addView(cart,parent.indexOfChild(findViewById(R.id.btnBuy)));
            cart.setOnClickListener(v->{extra.addCart(actor.id,id);Ui.toast(this,"Đã thêm giỏ hàng");});
            Button fav=ExtraUi.button(this,"♡ Thêm vào yêu thích");parent.addView(fav,parent.indexOfChild(findViewById(R.id.btnBuy)));
            fav.setOnClickListener(v->{extra.addFavorite(actor.id,id);Ui.toast(this,"Đã lưu yêu thích");});
            Button rate=ExtraUi.button(this,"★ Đánh giá gói (sau khi mua)");parent.addView(rate,parent.indexOfChild(findViewById(R.id.btnBuy)));
            rate.setOnClickListener(v->{
                if(!extra.hasAccess(actor.id,id)){Ui.toast(this,"Chỉ đánh giá sau khi kích hoạt gói");return;}
                String[] labels={"1 sao","2 sao","3 sao","4 sao","5 sao"};
                new AlertDialog.Builder(this).setTitle("Đánh giá: "+p.name).setItems(labels,(dialog,choice)->{
                    EditText comment=Ui.field(this,"Nhận xét của bạn","",InputType.TYPE_CLASS_TEXT);
                    new AlertDialog.Builder(this).setTitle((choice+1)+" sao").setView(comment).setPositiveButton("Gửi",(d,w)->{extra.ratePackage(actor.id,id,choice+1,Ui.value(comment));meta.setText(extra.details(id)+"\n"+extra.packageRating(id));}).setNegativeButton("Hủy",null).show();
                }).show();
            });
            Button learn=ExtraUi.button(this,"🎓 Học thử (nếu có link)");parent.addView(learn,parent.indexOfChild(findViewById(R.id.btnBuy)));
            learn.setOnClickListener(v->{String u=extra.trialUrl(id);if(!u.startsWith("https://")){Ui.toast(this,"Gói chưa có link học thử");return;}startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));});
        }else{
            Button adminBtn=ExtraUi.button(this,"⚙️ Chỉnh sửa toàn bộ thuộc tính gói");
            parent.addView(adminBtn,parent.indexOfChild(findViewById(R.id.btnBuy)));
            adminBtn.setOnClickListener(v->{
                Intent intent=new Intent(this,PackagesActivity.class);
                intent.putExtra("edit_package_id",id);
                startActivity(intent);
            });
        }
        if(!p.active || "ADMIN".equals(actor.role)){
            findViewById(R.id.btnBuy).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.btnBuy).setOnClickListener(v->{
            long orderId=db.createOrder(actor.id,p.id);
            if(orderId<1){Ui.toast(this,"Không thể tạo đơn hàng");return;}
            Intent intent=new Intent(this,PaymentActivity.class);
            intent.putExtra("order_id",orderId);startActivity(intent);finish();
        });
    }
    @Override protected void onResume(){
        super.onResume();
        // Quay lại từ màn hình chỉnh sửa để tải giá trị mới nhất từ SQLite.
        DbHelper db=new DbHelper(this);
        Models.PackageItem p=db.getPackage(getIntent().getLongExtra("package_id",-1));
        if(p==null)return;
        TextView name=findViewById(R.id.txtName);
        if(name!=null)name.setText(p.name);
        TextView info=findViewById(R.id.txtInfo);
        if(info!=null){
            String priceText=p.salePrice>0&&p.salePrice<p.price?"Giá gốc: "+Ui.money(p.price)+"\nGiá ưu đãi: "+Ui.money(p.salePrice):"Giá: "+Ui.money(p.price);
            info.setText("Mã gói: "+p.packageCode+"\nDanh mục: "+p.category+"\n"+priceText+"\nThời hạn: "+p.durationDays+" ngày • "+p.lessonCount+" bài học");
        }
        TextView desc=findViewById(R.id.txtDescription);
        if(desc!=null)desc.setText("Mô tả:\n"+p.description);
        TextView benefits=findViewById(R.id.txtBenefits);
        if(benefits!=null)benefits.setText(p.benefits.isEmpty()?"Quyền lợi đang được cập nhật":"✓  "+p.benefits.replace("\n","\n✓  "));
        TextView support=findViewById(R.id.txtSupportChannel);
        if(support!=null)support.setText("Hình thức hỗ trợ: "+p.supportChannel);
        if(extraInfoText!=null){
            ExtraStore extra=new ExtraStore(db);
            extraInfoText.setText(extra.details(p.id)+"\n"+extra.packageRating(p.id));
        }
    }
}
