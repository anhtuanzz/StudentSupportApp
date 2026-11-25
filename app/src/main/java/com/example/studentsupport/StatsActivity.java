package com.example.studentsupport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/** Người 5: thống kê thu nhập mô phỏng chỉ admin được xem. */
public final class StatsActivity extends Activity {
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_stats); Ui.bindNavigation(this,"profile");
        DbHelper db=new DbHelper(this);Models.User admin=Session.require(this,db);if(admin==null)return;
        if(!"ADMIN".equals(admin.role)){Ui.toast(this,"Chỉ Admin được xem thống kê");finish();return;}
        ((TextView)findViewById(R.id.txtStats)).setText(db.statsText());
        ((TextView)findViewById(R.id.txtBreakdown)).setText(db.revenueByPackage());
    }
}
