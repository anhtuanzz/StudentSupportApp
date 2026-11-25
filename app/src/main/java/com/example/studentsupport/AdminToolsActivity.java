package com.example.studentsupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

/** Các công cụ admin mở rộng: khóa tài khoản, mã voucher, học liệu và quiz. */
public final class AdminToolsActivity extends Activity {
    private DbHelper db;private ExtraStore store;private Models.User admin;private LinearLayout content;
    @Override protected void onCreate(Bundle s){super.onCreate(s);db=new DbHelper(this);admin=Session.require(this,db);if(admin==null)return;
        if(!"ADMIN".equals(admin.role)){Ui.toast(this,"Chỉ quản trị viên có quyền");finish();return;}
        store=new ExtraStore(db);home();
    }
    private void home(){
        content=ExtraUi.page(this,"🛠 Trung tâm quản trị","Quản lý người dùng, voucher, học liệu và câu hỏi. Dữ liệu lưu trực tiếp trong SQLite.");
        ExtraUi.addAction(this,content,"Quản lý tài khoản sinh viên",v->users());
        ExtraUi.addAction(this,content,"Mã khuyến mãi / Voucher",v->vouchers());
        ExtraUi.addAction(this,content,"Thêm tài liệu / video / link",v->editResource());
        ExtraUi.addAction(this,content,"Tạo câu hỏi trắc nghiệm",v->editQuiz());
        ExtraUi.card(this,content,"Các chức năng khác","Thêm/sửa/ẩn gói: tab Gói học (nhấn giữ gói).\nĐơn và doanh thu: tab Đơn hàng / Báo cáo.\nDuyệt hỗ trợ: tab Hỗ trợ.",null);
    }
    private void users(){
        content=ExtraUi.page(this,"👥 Quản lý sinh viên","Nhấn tài khoản sinh viên để khóa hoặc mở khóa đăng nhập.");
        for(String[] u:store.users()){
            long uid=Long.parseLong(u[0]);boolean locked=u[4].equals("1");
            ExtraUi.card(this,content,u[1]+" · "+u[3],u[2]+"\n"+(locked?"Đã khóa":"Đang hoạt động"),v->{
                if(!u[3].equals("STUDENT")){Ui.toast(this,"Không khóa tài khoản Admin");return;}
                new AlertDialog.Builder(this).setMessage((locked?"Mở khóa":"Khóa")+" đăng nhập cho "+u[1]+"?")
                    .setPositiveButton("Đồng ý",(d,w)->{store.setLocked(uid,!locked);users();}).setNegativeButton("Hủy",null).show();
            });
        }
    }
    private void vouchers(){
        content=ExtraUi.page(this,"🎟️ Voucher","Mã nhập khi tạo đơn trong giỏ hàng. Mã mới hết hạn sau 1 năm.");
        for(String row:store.vouchers())ExtraUi.card(this,content,row,"",null);
        EditText code=Ui.field(this,"Mã voucher, VD: FINAL30","",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText pct=Ui.field(this,"Giảm bao nhiêu phần trăm (1–90)","",InputType.TYPE_CLASS_NUMBER);
        content.addView(code);content.addView(pct);
        ExtraUi.addAction(this,content,"Tạo / cập nhật voucher",v->{
            try{int percent=Integer.parseInt(Ui.value(pct));if(!Ui.value(code).matches("[A-Za-z0-9_-]{3,24}")||percent<1||percent>90){Ui.toast(this,"Mã 3–24 ký tự; giảm 1–90%");return;}
                store.saveVoucher(Ui.value(code),percent,System.currentTimeMillis()+365L*86400000L);vouchers();
            }catch(NumberFormatException e){Ui.toast(this,"Nhập phần trăm là số nguyên");}
        });
    }
    private void editResource(){
        content=ExtraUi.page(this,"📁 Thêm học liệu","Bản demo lưu đường dẫn HTTPS của video/PDF (không upload tệp lên máy chủ).");
        List<Models.PackageItem> ps=db.packages("","Tất cả danh mục");
        for(Models.PackageItem p:ps){
            ExtraUi.card(this,content,p.name,"Chạm để thêm bài học",v->resourceDialog(p));
        }
    }
    private void resourceDialog(Models.PackageItem p){
        LinearLayout form=Ui.form(this);
        EditText title=Ui.field(this,"Tên bài học","",InputType.TYPE_CLASS_TEXT);
        EditText url=Ui.field(this,"Link HTTPS video / PDF / tài liệu","https://",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        form.addView(title);form.addView(url);
        new AlertDialog.Builder(this).setTitle("Học liệu: "+p.name).setView(form)
            .setPositiveButton("Lưu link",(d,w)->{
                if(Ui.value(title).isEmpty()||!Ui.value(url).startsWith("https://")){Ui.toast(this,"Cần tên và URL HTTPS hợp lệ");return;}
                store.addResource(p.id,Ui.value(title),"LINK",Ui.value(url));Ui.toast(this,"Đã lưu học liệu");
            }).setNegativeButton("Hủy",null).show();
    }
    private void editQuiz(){
        content=ExtraUi.page(this,"📝 Tạo quiz","Mỗi câu hỏi có 4 đáp án. Đáp án đúng nhập 1–4.");
        for(Models.PackageItem p:db.packages("","Tất cả danh mục"))ExtraUi.card(this,content,p.name,"Thêm câu hỏi cho gói",v->quizDialog(p));
    }
    private void quizDialog(Models.PackageItem p){
        LinearLayout f=Ui.form(this);
        EditText q=Ui.field(this,"Câu hỏi","",InputType.TYPE_CLASS_TEXT);
        EditText a=Ui.field(this,"Đáp án A","",InputType.TYPE_CLASS_TEXT);
        EditText b=Ui.field(this,"Đáp án B","",InputType.TYPE_CLASS_TEXT);
        EditText c=Ui.field(this,"Đáp án C","",InputType.TYPE_CLASS_TEXT);
        EditText d=Ui.field(this,"Đáp án D","",InputType.TYPE_CLASS_TEXT);
        EditText right=Ui.field(this,"Đáp án đúng (1–4)","",InputType.TYPE_CLASS_NUMBER);
        for(EditText e:new EditText[]{q,a,b,c,d,right})f.addView(e);
        new AlertDialog.Builder(this).setTitle("Quiz: "+p.name).setView(f).setPositiveButton("Lưu câu hỏi",(dialog,w)->{
            try{int idx=Integer.parseInt(Ui.value(right))-1;
                if(idx<0||idx>3||Ui.value(q).isEmpty()||Ui.value(a).isEmpty()||Ui.value(b).isEmpty()||Ui.value(c).isEmpty()||Ui.value(d).isEmpty()){Ui.toast(this,"Nhập đủ nội dung và đáp án đúng 1–4");return;}
                store.addQuestion(p.id,Ui.value(q),Ui.value(a),Ui.value(b),Ui.value(c),Ui.value(d),idx);Ui.toast(this,"Đã tạo câu hỏi");
            }catch(NumberFormatException e){Ui.toast(this,"Đáp án đúng phải là 1, 2, 3 hoặc 4");}
        }).setNegativeButton("Hủy",null).show();
    }
}
