package com.example.studentsupport;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.View;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Chatbot FAQ offline chạy được; AI thật chỉ chạy khi người triển khai cung cấp HTTPS backend. */
public final class ChatbotActivity extends Activity {
    private ExtraStore store;private Models.User user;private LinearLayout history;
    private String endpoint;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);DbHelper db=new DbHelper(this);user=Session.require(this,db);if(user==null)return;
        store=new ExtraStore(db);endpoint=getString(R.string.chat_backend_url).trim();
        LinearLayout content=ExtraUi.page(this,"💬 Trợ lý học tập",endpoint.isEmpty()?"Chế độ FAQ offline (chưa kết nối AI trực tuyến).":"Đã cấu hình backend AI HTTPS – câu trả lời cần được kiểm tra.");
        ExtraUi.card(this,content,"Bạn có thể hỏi:","• Gói TOEIC có gì?\n• Cách ôn thi hiệu quả\n• Gia hạn gói như thế nào?\n• Liên hệ hỗ trợ ở đâu?",null);
        history=new LinearLayout(this);history.setOrientation(LinearLayout.VERTICAL);content.addView(history);
        EditText field=Ui.field(this,"Nhập câu hỏi của bạn...","",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);content.addView(field);
        ExtraUi.addAction(this,content,"Gửi câu hỏi",v->{
            String question=Ui.value(field);
            if(question.isEmpty())return;if(question.length()>800){Ui.toast(this,"Câu hỏi tối đa 800 ký tự");return;}
            field.setText("");store.chat(user.id,"USER",question);draw();
            if(endpoint.startsWith("https://"))askBackend(question);else{store.chat(user.id,"ASSISTANT",offlineAnswer(question,db));draw();}
        });
        draw();
    }
    private void draw(){
        history.removeAllViews();List<String[]> data=store.chatHistory(user.id);
        if(data.isEmpty())ExtraUi.card(this,history,"Chào bạn!","Mình là trợ lý hướng dẫn offline. Hãy chọn một câu hỏi hoặc nhập nội dung cần hỗ trợ.",null);
        for(String[] row:data){
            boolean me=row[0].equals("USER");
            TextView bubble=Ui.makeText(this,(me?"Bạn: ":"Trợ lý: ")+row[1],14,Ui.INK,me);
            bubble.setPadding(Ui.dp(this,13),Ui.dp(this,11),Ui.dp(this,13),Ui.dp(this,11));
            bubble.setBackgroundResource(me?R.drawable.hero_peach:R.drawable.card_bg);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=Ui.dp(this,9);history.addView(bubble,lp);
        }
        history.post(()->{View p=(View)history.getParent();if(p instanceof ScrollView)((ScrollView)p).fullScroll(View.FOCUS_DOWN);});
    }
    private String offlineAnswer(String text,DbHelper db){
        String q=text.toLowerCase(new Locale("vi","VN"));
        if(q.contains("toeic")||q.contains("tiếng anh")||q.contains("gói học")||q.contains("học phí")){
            List<Models.PackageItem> packs=db.packages(q.contains("toeic")?"TOEIC":"","Tất cả danh mục");
            if(!packs.isEmpty()){Models.PackageItem p=packs.get(0);return "Gói gợi ý: "+p.name+" ("+Ui.money(p.price)+", "+p.durationDays+" ngày). Vào Gói học để xem chi tiết và mua.";}
            return "Bạn vào mục Gói học, tìm bằng tên môn hoặc bộ lọc để chọn gói phù hợp.";
        }
        if(q.contains("ôn thi")||q.contains("học tập")||q.contains("ghi nhớ"))return "Gợi ý phương pháp: chia nhỏ kiến thức, học 25 phút rồi nghỉ 5 phút, luyện đề sau từng chủ đề và ôn cách quãng 1-3-7 ngày. Bạn có thể học Quiz trong mục Không gian học tập.";
        if(q.contains("gia hạn")||q.contains("hết hạn"))return "Mở Gói đang sử dụng trên Trang chủ, chọn gói cần gia hạn, tạo đơn mới và xác nhận thanh toán mô phỏng.";
        if(q.contains("thanh toán")||q.contains("đơn hàng"))return "Mở Đơn hàng, chọn đơn đang chờ và nhấn Thanh toán QR mô phỏng. Bản demo không chuyển tiền thật.";
        if(q.contains("hỗ trợ")||q.contains("mentor")||q.contains("giảng viên"))return "Vào tab Hỗ trợ để gửi yêu cầu cho quản trị viên. Bạn cũng có thể hỏi về nội dung học phần đang sử dụng.";
        return "Mình là chatbot FAQ offline, chưa phải mô hình AI sinh văn bản. Bạn có thể hỏi về gói học, ôn thi, đơn hàng, gia hạn hoặc hỗ trợ. AI thực cần cấu hình backend HTTPS riêng.";
    }
    private void askBackend(String message){
        new Thread(()->{
            String answer;
            try{
                URL url=new URL(endpoint);HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(8000);connection.setReadTimeout(16000);connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/json; charset=utf-8");connection.setDoOutput(true);
                byte[] req=new JSONObject().put("message",message).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream o=connection.getOutputStream()){o.write(req);}
                if(connection.getResponseCode()!=200)throw new IllegalStateException("HTTP "+connection.getResponseCode());
                StringBuilder b=new StringBuilder();try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=br.readLine())!=null){b.append(line);if(b.length()>20000)break;}}
                answer=new JSONObject(b.toString()).optString("reply","");if(answer.trim().isEmpty())throw new IllegalStateException("Phản hồi trống");
                connection.disconnect();
            }catch(Exception e){answer="Không kết nối được máy chủ AI. Hãy kiểm tra backend HTTPS. ("+e.getClass().getSimpleName()+")";}
            final String result=answer;
            runOnUiThread(()->{store.chat(user.id,"ASSISTANT",result);draw();});
        }).start();
    }
}
