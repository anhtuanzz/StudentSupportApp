package com.example.studentsupport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.text.InputType;

public final class ProfileActivity extends Activity {
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_profile); Ui.bindNavigation(this,"profile");
        DbHelper db=new DbHelper(this);Models.User u=Session.require(this,db);if(u==null)return;
        ((TextView)findViewById(R.id.txtEmail)).setText("Email: "+u.email);
        ((TextView)findViewById(R.id.txtCode)).setText("Mã SV: "+(u.studentCode==null?"(Admin)":u.studentCode));
        EditText name=findViewById(R.id.edtName),phone=findViewById(R.id.edtPhone);
        name.setText(u.name);phone.setText(u.phone);
        ExtraStore extra=new ExtraStore(db);
        String[] profile=extra.profile(u.id);
        LinearLayout parent=(LinearLayout)findViewById(R.id.btnSave).getParent();
        EditText school=Ui.field(this,"Trường đại học",profile[0],InputType.TYPE_CLASS_TEXT);
        EditText major=Ui.field(this,"Chuyên ngành",profile[1],InputType.TYPE_CLASS_TEXT);
        EditText cohort=Ui.field(this,"Khóa học / niên khóa",profile[2],InputType.TYPE_CLASS_TEXT);
        for(EditText e:new EditText[]{school,major,cohort})parent.addView(e,parent.indexOfChild(findViewById(R.id.btnSave)));
        ExtraUi.addAction(this,parent,"📚 Không gian học tập",v->startActivity(new android.content.Intent(this,LearningActivity.class)));
        ExtraUi.addAction(this,parent,"💬 Chatbot trợ lý",v->startActivity(new android.content.Intent(this,ChatbotActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v->Session.logout(this));
        findViewById(R.id.btnSave).setOnClickListener(v->{
            if(db.updateProfile(u.id,Ui.value(name),Ui.value(phone))){extra.saveProfile(u.id,Ui.value(school),Ui.value(major),Ui.value(cohort));Ui.toast(this,"Cập nhật thông tin thành công");}
            else Ui.toast(this,"Không thể cập nhật, vui lòng nhập họ tên");
        });
    }
}
