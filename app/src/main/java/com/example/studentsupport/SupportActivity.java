package com.example.studentsupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/** Người 5: sinh viên tạo phiếu/đánh giá; admin tiếp nhận và xử lý phiếu. */
public final class SupportActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    private List<Models.TicketItem> rows=new ArrayList<>();
    private ListView list;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_support); Ui.bindNavigation(this,"support");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        list=findViewById(R.id.listTickets);
        findViewById(R.id.btnSupportChatbot).setOnClickListener(v->startActivity(new Intent(this,ChatbotActivity.class)));
        if("ADMIN".equals(actor.role))findViewById(R.id.btnAddTicket).setVisibility(View.GONE);
        else findViewById(R.id.btnAddTicket).setOnClickListener(v->newTicket());
        list.setOnItemClickListener((p,v,index,id)->{if(index<rows.size())showTicket(rows.get(index));});
    }
    private void newTicket(){
        LinearLayout form=Ui.form(this);
        EditText title=Ui.field(this,"Tiêu đề hỗ trợ","",InputType.TYPE_CLASS_TEXT);
        EditText message=Ui.field(this,"Mô tả vấn đề cần hỗ trợ","",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(title);form.addView(message);
        new AlertDialog.Builder(this).setTitle("Gửi yêu cầu")
            .setView(form).setPositiveButton("Gửi",(d,w)->{
                long id=db.createTicket(actor.id,Ui.value(title),Ui.value(message));
                Ui.toast(this,id>0?"Đã gửi yêu cầu #"+id:"Nhập đầy đủ tiêu đề và nội dung");refresh();
            }).setNegativeButton("Hủy",null).show();
    }
    private void showTicket(Models.TicketItem t){
        String message="Sinh viên: "+t.userName+"\nNgày: "+Ui.date(t.createdAt)+
            "\nTrạng thái: "+t.status+"\n\nYêu cầu:\n"+t.message+
            "\n\nPhản hồi Admin:\n"+(t.reply.isEmpty()?"(Chưa có)":t.reply)+
            "\n\nĐánh giá: "+(t.rating==0?"Chưa đánh giá":t.rating+"/5 sao");
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("#"+t.id+" — "+t.subject).setMessage(message).setNegativeButton("Đóng",null);
        if("ADMIN".equals(actor.role)){
            builder.setPositiveButton("Xử lý / trả lời",(d,w)->replyTicket(t));
        }else if("RESOLVED".equals(t.status) && t.rating==0){
            builder.setPositiveButton("Đánh giá",(d,w)->rateTicket(t));
        }
        builder.show();
    }
    private void replyTicket(Models.TicketItem t){
        EditText reply=Ui.field(this,"Nội dung trả lời",t.reply,InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        new AlertDialog.Builder(this).setTitle("Tiếp nhận & xử lý yêu cầu #"+t.id).setView(reply)
            .setPositiveButton("Gửi & hoàn tất",(d,w)->{
                boolean ok=db.resolveTicket(actor,t.id,Ui.value(reply));
                Ui.toast(this,ok?"Yêu cầu đã được giải quyết":"Cần nhập nội dung phản hồi");refresh();
            }).setNegativeButton("Hủy",null).show();
    }
    private void rateTicket(Models.TicketItem t){
        String[] stars={"1 sao","2 sao","3 sao","4 sao","5 sao"};
        final int[] selected={4};
        new AlertDialog.Builder(this).setTitle("Đánh giá dịch vụ")
            .setSingleChoiceItems(stars,4,(d,which)->selected[0]=which)
            .setPositiveButton("Gửi đánh giá",(d,which)->{
                boolean ok=db.rateTicket(actor.id,t.id,selected[0]+1,"");
                Ui.toast(this,ok?"Cảm ơn bạn đã đánh giá":"Không thể gửi đánh giá");refresh();
            }).setNegativeButton("Hủy",null).show();
    }
    private void refresh(){
        rows=db.tickets(actor);
        List<String> labels=new ArrayList<>();
        for(Models.TicketItem t:rows){
            String owner="ADMIN".equals(actor.role)?" • "+t.userName:"";
            labels.add("#"+t.id+" • "+t.subject+owner+"\n"+t.status+" • "+Ui.date(t.createdAt));
        }
        if(rows.isEmpty())labels.add("Chưa có yêu cầu hỗ trợ");
        list.setAdapter(Ui.listAdapter(this,labels));
    }
    @Override protected void onResume(){super.onResume();if(db!=null&&actor!=null&&list!=null)refresh();}
}
