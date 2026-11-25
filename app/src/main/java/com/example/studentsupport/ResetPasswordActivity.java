package com.example.studentsupport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public final class ResetPasswordActivity extends Activity {
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_reset); Ui.styleWindow(this); findViewById(R.id.btnBack).setOnClickListener(v->finish());
        DbHelper db=new DbHelper(this);
        EditText email=findViewById(R.id.edtEmail),code=findViewById(R.id.edtCode),pass=findViewById(R.id.edtNewPassword);
        findViewById(R.id.btnReset).setOnClickListener(v->{
            if(db.resetPassword(Ui.value(email),Ui.value(code),pass.getText().toString())){
                Ui.toast(this,"Đã đặt lại mật khẩu, hãy đăng nhập");finish();
            } else Ui.toast(this,"Thông tin không đúng hoặc mật khẩu dưới 8 ký tự");
        });
    }
}
