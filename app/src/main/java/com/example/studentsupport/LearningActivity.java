package com.example.studentsupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.util.List;

/** Học liệu URL, tiến độ và quiz thật trên SQLite; chỉ xem sau khi có subscription. */
public final class LearningActivity extends Activity {
    private DbHelper db;private ExtraStore store;private Models.User user;private LinearLayout content;
    @Override protected void onCreate(Bundle s){super.onCreate(s);db=new DbHelper(this);user=Session.require(this,db);if(user==null)return;store=new ExtraStore(db);build();}
    private void build(){
        content=ExtraUi.page(this,"📚 Không gian học tập","Tài liệu, video/link và câu hỏi trắc nghiệm của các gói đã kích hoạt.");
        List<Models.SubscriptionItem> packs=db.subscriptions(user.id);long now=System.currentTimeMillis();int count=0;
        for(Models.SubscriptionItem sub:packs){if(sub.expiresAt<=now)continue;count++;
            ExtraUi.card(this,content,sub.packageName,"Tiến độ: "+store.progress(user.id,sub.packageId)+"% · Hết hạn "+Ui.date(sub.expiresAt),v->showResources(sub.packageId,sub.packageName));
        }
        if(count==0)ExtraUi.card(this,content,"Chưa có gói đang hoạt động","Mở Gói học, chọn mua và hoàn tất thanh toán demo để truy cập học liệu.",null);
    }
    private void showResources(long pid,String name){
        content=ExtraUi.page(this,"📚 "+name,"Chạm nội dung để mở đường dẫn tài liệu; nhấn Đã hoàn thành để lưu tiến độ.");
        for(String[] row:store.resources(pid)){
            long rid=Long.parseLong(row[0]);
            String info=row[2]+"  •  "+(store.isCompleted(user.id,rid)?"✓ Hoàn thành":"Chưa hoàn thành");
            ExtraUi.card(this,content,row[1],info+"\n"+row[3],v->{
                new AlertDialog.Builder(this).setTitle(row[1]).setItems(new String[]{"Mở học liệu","Đánh dấu đã hoàn thành"},(d,w)->{
                    if(w==0){Uri uri=Uri.parse(row[3]);if(!"https".equals(uri.getScheme())&&!"http".equals(uri.getScheme())){Ui.toast(this,"Chỉ hỗ trợ link HTTP/HTTPS");return;}
                        Intent i=new Intent(Intent.ACTION_VIEW,uri);
                        if(i.resolveActivity(getPackageManager())!=null)startActivity(i);else Ui.toast(this,"Không có ứng dụng để mở liên kết");}
                    else{store.complete(user.id,rid);showResources(pid,name);}
                }).show();
            });
        }
        ExtraUi.addAction(this,content,"Làm bài Quiz",v->quiz(pid));
        ExtraUi.addAction(this,content,"Gửi câu hỏi cho mentor (phiếu hỗ trợ)",v->startActivity(new Intent(this,SupportActivity.class)));
    }
    private void quiz(long pid){
        List<String[]> data=store.questions(pid);if(data.isEmpty()){Ui.toast(this,"Gói này chưa có câu hỏi");return;}
        String[] row=data.get(0);String[] answers={row[2],row[3],row[4],row[5]};int right=Integer.parseInt(row[6]);
        new AlertDialog.Builder(this).setTitle("Quiz · "+row[1]).setSingleChoiceItems(answers,-1,(dialog,index)->{
            dialog.dismiss();boolean correct=index==right;store.quizAnswer(user.id,Long.parseLong(row[0]),correct);
            new AlertDialog.Builder(this).setTitle(correct?"✓ Chính xác":"Chưa chính xác")
                .setMessage("Đáp án đúng: "+answers[right]+"\nKết quả đã được lưu vào SQLite.").setPositiveButton("OK",null).show();
        }).setNegativeButton("Bỏ qua",null).show();
    }
}
