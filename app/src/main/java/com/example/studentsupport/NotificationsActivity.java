package com.example.studentsupport;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.util.List;

/** Thông báo trong app: tạo khi mở màn hình, chưa có push/background scheduler. */
public final class NotificationsActivity extends Activity {
    private ExtraStore store;private Models.User user;private DbHelper db;
    @Override protected void onCreate(Bundle s){super.onCreate(s);db=new DbHelper(this);user=Session.require(this,db);if(user==null)return;store=new ExtraStore(db);
        long now=System.currentTimeMillis();long threshold=7L*86400000L;
        for(Models.SubscriptionItem sub:db.subscriptions(user.id)){
            long remain=sub.expiresAt-now;
            if(remain>0&&remain<threshold)store.notifyUser(user.id,"expiring-"+sub.id+"-"+sub.expiresAt,"Gói '"+sub.packageName+"' còn dưới 7 ngày. Hãy gia hạn nếu cần.");
            else if(remain<=0)store.notifyUser(user.id,"expired-"+sub.id+"-"+sub.expiresAt,"Gói '"+sub.packageName+"' đã hết hạn.");
        }
        LinearLayout view=ExtraUi.page(this,"🔔 Thông báo","Thông báo lưu trong ứng dụng, được kiểm tra khi bạn mở màn hình này.");
        List<String[]> data=store.notifications(user.id);
        for(String[] item:data){long id=Long.parseLong(item[0]);
            ExtraUi.card(this,view,item[2].equals("0")?"● Chưa đọc":"Đã đọc",item[1],v->{store.markRead(user.id,id);recreate();});
        }
        if(data.isEmpty())ExtraUi.card(this,view,"Không có thông báo","Bạn sẽ thấy nhắc hạn gói ở đây khi đến gần ngày hết hạn.",null);
    }
}
