package com.example.studentsupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

/** Người 1: đăng nhập, đăng xuất (qua Session), điều hướng đăng ký/quên mật khẩu. */
public final class LoginActivity extends Activity {
    private DbHelper db;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login); Ui.styleWindow(this);
        try {DbInstaller.ensureInstalled(this);db=new DbHelper(this);db.getReadableDatabase();}
        catch(Exception e){Ui.toast(this,"Lỗi mở CSDL: "+e.getMessage());return;}
        if(Session.userId(this)>0 && db.getUser(Session.userId(this))!=null) {
            startActivity(new Intent(this,HomeActivity.class));finish();return;
        }
        EditText email=findViewById(R.id.edtEmail), password=findViewById(R.id.edtPassword);
        // Điền chính xác tài khoản mẫu; người học vẫn phải nhấn Đăng nhập.
        findViewById(R.id.btnDemoStudent).setOnClickListener(v->{email.setText("sv01@demo.local");password.setText("Student@123");});
        findViewById(R.id.btnDemoAdmin).setOnClickListener(v->{email.setText("admin@demo.local");password.setText("Admin@123");});
        findViewById(R.id.btnLogin).setOnClickListener(v->{
            Models.User u=db.authenticate(Ui.value(email),password.getText().toString());
            if(u==null){Ui.toast(this,"Email hoặc mật khẩu không đúng");return;}
            Session.login(this,u.id);
            startActivity(new Intent(this,HomeActivity.class));finish();
        });
        findViewById(R.id.btnRegister).setOnClickListener(v->startActivity(new Intent(this,RegisterActivity.class)));
        findViewById(R.id.btnReset).setOnClickListener(v->startActivity(new Intent(this,ResetPasswordActivity.class)));
    }
}
