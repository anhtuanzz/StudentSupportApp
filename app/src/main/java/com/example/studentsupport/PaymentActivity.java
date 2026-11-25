package com.example.studentsupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/** Người 4: QR chỉ phục vụ demo, tuyệt đối không xác thực chuyển khoản thật. */
public final class PaymentActivity extends Activity {
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_payment); Ui.bindNavigation(this,"orders");
        DbHelper db=new DbHelper(this);Models.User user=Session.require(this,db);if(user==null)return;
        Models.OrderItem order=db.getOrder(getIntent().getLongExtra("order_id",-1));
        if(order==null || order.userId!=user.id || !"PENDING".equals(order.status)){
            Ui.toast(this,"Đơn không hợp lệ hoặc đã xử lý");finish();return;
        }
        ((TextView)findViewById(R.id.txtPayment)).setText("Mã đơn: #"+order.id+"\nGói: "+order.packageName+
            "\nSố tiền mô phỏng: "+Ui.money(order.amount));
        // QR chứa chuỗi DEMO, không chứa số tài khoản ngân hàng hoặc yêu cầu thanh toán thật.
        String payload="STUDENT_SUPPORT_DEMO|ORDER="+order.id+"|AMOUNT_VND="+order.amount;
        try {
            BitMatrix matrix=new MultiFormatWriter().encode(payload,BarcodeFormat.QR_CODE,400,400);
            Bitmap image=Bitmap.createBitmap(matrix.getWidth(),matrix.getHeight(),Bitmap.Config.ARGB_8888);
            for(int y=0;y<matrix.getHeight();y++)for(int x=0;x<matrix.getWidth();x++){
                image.setPixel(x,y,matrix.get(x,y)?Color.BLACK:Color.WHITE);
            }
            ((ImageView)findViewById(R.id.imgQr)).setImageBitmap(image);
        }catch(Exception e){Ui.toast(this,"Không tạo được QR: "+e.getMessage());}
        findViewById(R.id.btnConfirm).setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Xác nhận GIẢ LẬP")
            .setMessage("Thao tác này sẽ đánh dấu thanh toán thử nghiệm là thành công và kích hoạt/gia hạn gói. Không có tiền thật được chuyển.")
            .setPositiveButton("Đồng ý mô phỏng",(dialog,which)->{
                if(db.payDemo(user.id,order.id)){
                    Ui.toast(this,"Thanh toán DEMO thành công — đã kích hoạt/gia hạn gói");finish();
                } else Ui.toast(this,"Đơn đã được xử lý hoặc có lỗi cơ sở dữ liệu");
            }).setNegativeButton("Hủy",null).show());
    }
}
