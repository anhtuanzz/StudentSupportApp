package com.example.studentsupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/** Người 2: danh sách, tìm kiếm, lọc danh mục và CRUD gói cho admin. */
public final class PackagesActivity extends Activity {
    private DbHelper db;
    private Models.User actor;
    private List<Models.PackageItem> current=new ArrayList<>();
    private int maxPrice=0,maxDays=0,minStars=0;
    private EditText search;
    private Spinner filter;
    private ListView list;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_packages); Ui.bindNavigation(this,"packages");
        db=new DbHelper(this);actor=Session.require(this,db);if(actor==null)return;
        search=findViewById(R.id.edtSearch);filter=findViewById(R.id.spFilter);list=findViewById(R.id.listPackages);
        android.widget.Button advanced=ExtraUi.button(this,"⚙ Lọc giá · thời hạn · đánh giá");
        ((LinearLayout)search.getParent()).addView(advanced);
        advanced.setOnClickListener(v->advancedFilter());
        refreshCategories();
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){refreshList();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){refreshList();}
            @Override public void afterTextChanged(Editable s){}
        });
        list.setOnItemClickListener((p,v,index,id)->{
            if(index>=current.size())return;
            Intent i=new Intent(this,PackageDetailActivity.class);i.putExtra("package_id",current.get(index).id);startActivity(i);
        });
        if("ADMIN".equals(actor.role)){
            findViewById(R.id.btnAddPackage).setVisibility(View.VISIBLE);
            findViewById(R.id.btnAddPackage).setOnClickListener(v->editPackage(null));
            list.setOnItemLongClickListener((p,v,pos,id)->{
                if(pos>=current.size())return true;
                Models.PackageItem item=current.get(pos);
                new AlertDialog.Builder(this).setTitle(item.name).setItems(new String[]{"Sửa gói","Xóa gói"},(d,choice)->{
                    if(choice==0)editPackage(item);
                    else new AlertDialog.Builder(this).setMessage("Ẩn gói khỏi danh sách bán? Lịch sử đơn hàng vẫn được giữ lại.")
                        .setPositiveButton("Xóa",(a,b)->{if(db.deletePackage(actor,item.id)){refreshCategories();refreshList();}})
                        .setNegativeButton("Không",null).show();
                }).show();
                return true;
            });
        }
        refreshList();
        long editId=getIntent().getLongExtra("edit_package_id",-1);
        if(editId>0 && "ADMIN".equals(actor.role)){
            Models.PackageItem edit=db.getPackage(editId);
            if(edit!=null)editPackage(edit);
            getIntent().removeExtra("edit_package_id");
        }
    }
    private void refreshCategories(){
        String previous=filter.getSelectedItem()==null?"Tất cả danh mục":filter.getSelectedItem().toString();
        List<String> categories=db.categories();
        filter.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,categories));
        int index=categories.indexOf(previous);filter.setSelection(Math.max(index,0));
    }
    private void refreshList(){
        if(filter.getSelectedItem()==null)return;
        current=db.packages(Ui.value(search),filter.getSelectedItem().toString());
        ExtraStore extra=new ExtraStore(db);
        current.removeIf(p->(maxPrice>0&&extra.price(actor,p,0)>maxPrice)||(maxDays>0&&p.durationDays>maxDays)||
                (minStars>0&&extra.averageStars(p.id)<minStars));
        List<String> labels=new ArrayList<>();
        for(Models.PackageItem p:current)labels.add(p.name+"\n"+p.category+" • "+Ui.money(p.price)+" • "+p.durationDays+" ngày");
        if(labels.isEmpty())labels.add("Không có gói phù hợp");
        if(current.isEmpty())list.setAdapter(Ui.listAdapter(this,labels));
        else list.setAdapter(Ui.packageAdapter(this,current));
    }
    private void advancedFilter(){
        LinearLayout f=Ui.form(this);
        EditText price=Ui.field(this,"Giá tối đa VND (0 = tất cả)",""+maxPrice,InputType.TYPE_CLASS_NUMBER);
        EditText days=Ui.field(this,"Thời hạn tối đa số ngày (0 = tất cả)",""+maxDays,InputType.TYPE_CLASS_NUMBER);
        EditText stars=Ui.field(this,"Đánh giá tối thiểu 1–5 sao (0 = tất cả)",""+minStars,InputType.TYPE_CLASS_NUMBER);
        f.addView(price);f.addView(days);f.addView(stars);
        new AlertDialog.Builder(this).setTitle("Bộ lọc thông minh").setView(f).setPositiveButton("Áp dụng",(d,w)->{
            try{maxPrice=Math.max(0,Integer.parseInt(Ui.value(price)));maxDays=Math.max(0,Integer.parseInt(Ui.value(days)));
                minStars=Integer.parseInt(Ui.value(stars));if(minStars<0||minStars>5)minStars=0;refreshList();}
            catch(NumberFormatException e){Ui.toast(this,"Vui lòng nhập các số hợp lệ");}
        }).setNegativeButton("Hủy",null).show();
    }
    private EditText addField(LinearLayout layout,String caption,String initial,int inputType){
        TextView label=Ui.makeText(this,caption,13,Ui.INK,true);
        label.setPadding(0,Ui.dp(this,7),0,Ui.dp(this,5));
        layout.addView(label);
        EditText edit=Ui.field(this,caption,initial,inputType);
        layout.addView(edit);
        return edit;
    }
    private void editPackage(Models.PackageItem old){
        // ScrollView giúp nhập đủ thuộc tính trên màn hình điện thoại nhỏ.
        ScrollView scroll=new ScrollView(this);
        LinearLayout layout=Ui.form(this);
        scroll.addView(layout);
        int text=InputType.TYPE_CLASS_TEXT;
        int multi=text|InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        int number=InputType.TYPE_CLASS_NUMBER;
        EditText code=addField(layout,"Mã gói hỗ trợ (duy nhất)",old==null?"GOI-"+System.currentTimeMillis():old.packageCode,text|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText name=addField(layout,"Tên gói hỗ trợ",old==null?"":old.name,text);
        EditText category=addField(layout,"Danh mục / môn học",old==null?"":old.category,text);
        EditText price=addField(layout,"Giá gốc (VND)",old==null?"":String.valueOf(old.price),number);
        EditText sale=addField(layout,"Giá khuyến mãi (VND, 0 = không giảm)",old==null?"0":String.valueOf(old.salePrice),number);
        EditText days=addField(layout,"Thời hạn sử dụng (ngày)",old==null?"30":String.valueOf(old.durationDays),number);
        EditText lessons=addField(layout,"Số lượng bài học",old==null?"12":String.valueOf(old.lessonCount),number);
        EditText desc=addField(layout,"Mô tả gói hỗ trợ",old==null?"":old.description,multi);
        EditText target=addField(layout,"Đối tượng sinh viên phù hợp",old==null?"Sinh viên đại học, cao đẳng":old.targetStudents,text);
        EditText level=addField(layout,"Trình độ",old==null?"Cơ bản":old.level,text);
        EditText lecturer=addField(layout,"Giảng viên / mentor",old==null?"Giảng viên hỗ trợ":old.lecturer,text);
        EditText courseCode=addField(layout,"Mã học phần",old==null?"":old.courseCode,text);
        EditText path=addField(layout,"Lộ trình học tập",old==null?"Kiến thức nền → thực hành → kiểm tra":old.studyPath,multi);
        EditText benefits=addField(layout,"Quyền lợi khi đăng ký (mỗi ý một dòng)",old==null?"Bài giảng và tài liệu học tập\nBài tập ôn luyện và hỗ trợ giải đáp":old.benefits,multi);
        EditText support=addField(layout,"Hình thức hỗ trợ",old==null?"Chat trong ứng dụng, gửi yêu cầu hỗ trợ":old.supportChannel,multi);
        EditText requirements=addField(layout,"Yêu cầu kiến thức đầu vào",old==null?"Không yêu cầu":old.prerequisites,multi);
        EditText trial=addField(layout,"Liên kết học thử (HTTPS, có thể trống)",old==null?"":old.trialUrl,text|InputType.TYPE_TEXT_VARIATION_URI);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(old==null?"Thêm gói hỗ trợ":"Sửa thuộc tính gói")
            .setView(scroll).setPositiveButton("Lưu gói",null).setNegativeButton("Hủy",null).create();
        dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            try{
                Models.PackageItem item=new Models.PackageItem();
                item.id=old==null?0:old.id;
                item.packageCode=Ui.value(code).toUpperCase(Locale.ROOT);
                item.name=Ui.value(name);item.category=Ui.value(category);
                item.price=Integer.parseInt(Ui.value(price));
                item.salePrice=Integer.parseInt(Ui.value(sale));
                item.durationDays=Integer.parseInt(Ui.value(days));
                item.lessonCount=Integer.parseInt(Ui.value(lessons));
                item.description=Ui.value(desc);item.targetStudents=Ui.value(target);
                item.level=Ui.value(level);item.lecturer=Ui.value(lecturer);
                item.courseCode=Ui.value(courseCode);item.studyPath=Ui.value(path);
                item.benefits=Ui.value(benefits);item.supportChannel=Ui.value(support);
                item.prerequisites=Ui.value(requirements);item.trialUrl=Ui.value(trial);
                if(!item.trialUrl.isEmpty()&&!item.trialUrl.startsWith("https://")){
                    Ui.toast(this,"Đường dẫn học thử phải bắt đầu bằng https://");return;
                }
                if(db.savePackage(actor,item)){
                    Ui.toast(this,"Đã lưu toàn bộ thuộc tính gói vào SQLite");
                    refreshCategories();refreshList();dialog.dismiss();
                }else Ui.toast(this,"Kiểm tra mã gói trùng, tên/mô tả trống, giá giảm > giá gốc hoặc thời hạn không hợp lệ");
            }catch(NumberFormatException e){Ui.toast(this,"Giá, số ngày và số bài học phải là số nguyên");}
        }));
        dialog.show();
    }
    @Override protected void onResume(){super.onResume();if(db!=null&&filter!=null)refreshList();}
}
