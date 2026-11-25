package com.example.studentsupport;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;

public final class RegisterActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_register); Ui.styleWindow(this); findViewById(R.id.btnBack).setOnClickListener(v->finish());
        DbHelper db=new DbHelper(this);
        EditText name=findViewById(R.id.edtName),email=findViewById(R.id.edtEmail),code=findViewById(R.id.edtCode),phone=findViewById(R.id.edtPhone);
        EditText pass=findViewById(R.id.edtPassword),confirm=findViewById(R.id.edtConfirm);
        findViewById(R.id.btnSave).setOnClickListener(v->{
            String pw=pass.getText().toString();
            if(Ui.value(name).isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(Ui.value(email)).matches() || Ui.value(code).isEmpty() || pw.length()<8){
                Ui.toast(this,"Nhập đủ tên, email hợp lệ, mã SV và mật khẩu ít nhất 8 ký tự");return;
            }
            if(!pw.equals(confirm.getText().toString())){Ui.toast(this,"Hai mật khẩu không khớp");return;}
            if(db.register(Ui.value(name),Ui.value(email),Ui.value(code),Ui.value(phone),pw)){
                Ui.toast(this,Ui.value(email).toLowerCase().endsWith(".edu.vn")?"Đã đăng ký – email .edu.vn được hưởng ưu đãi demo 10% (chưa xác minh trường)":"Đăng ký thành công, hãy đăng nhập");finish();
            }else Ui.toast(this,"Không thể đăng ký: email hoặc mã SV đã tồn tại");
        });
    }
}
