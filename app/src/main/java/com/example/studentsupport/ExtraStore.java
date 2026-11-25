package com.example.studentsupport;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Các phân hệ mở rộng chạy offline, dùng cùng cơ sở dữ liệu SQLite v2. */
final class ExtraStore {
    private final DbHelper base;
    ExtraStore(DbHelper base){this.base=base;}
    private SQLiteDatabase db(){return base.getWritableDatabase();}
    static void createTables(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS student_profiles (user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, university TEXT NOT NULL DEFAULT '',major TEXT NOT NULL DEFAULT '',cohort TEXT NOT NULL DEFAULT '')");
        d.execSQL("CREATE TABLE IF NOT EXISTS favorites (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,package_id INTEGER NOT NULL REFERENCES packages(id),PRIMARY KEY(user_id,package_id))");
        d.execSQL("CREATE TABLE IF NOT EXISTS cart_items (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,package_id INTEGER NOT NULL REFERENCES packages(id),PRIMARY KEY(user_id,package_id))");
        d.execSQL("CREATE TABLE IF NOT EXISTS vouchers (code TEXT PRIMARY KEY,percent INTEGER NOT NULL CHECK(percent BETWEEN 1 AND 90),active INTEGER NOT NULL DEFAULT 1,expires_at INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS package_extras (package_id INTEGER PRIMARY KEY REFERENCES packages(id) ON DELETE CASCADE,lecturer TEXT NOT NULL DEFAULT '',course_code TEXT NOT NULL DEFAULT '',study_path TEXT NOT NULL DEFAULT '',trial_url TEXT NOT NULL DEFAULT '',sale_price INTEGER NOT NULL DEFAULT 0,package_code TEXT NOT NULL DEFAULT '',target_students TEXT NOT NULL DEFAULT '',level TEXT NOT NULL DEFAULT '',lesson_count INTEGER NOT NULL DEFAULT 0,support_channel TEXT NOT NULL DEFAULT '',benefits TEXT NOT NULL DEFAULT '',prerequisites TEXT NOT NULL DEFAULT '')");
        d.execSQL("CREATE TABLE IF NOT EXISTS package_ratings (user_id INTEGER NOT NULL REFERENCES users(id),package_id INTEGER NOT NULL REFERENCES packages(id),stars INTEGER NOT NULL CHECK(stars BETWEEN 1 AND 5),comment TEXT NOT NULL DEFAULT '',PRIMARY KEY(user_id,package_id))");
        d.execSQL("CREATE TABLE IF NOT EXISTS learning_resources (id INTEGER PRIMARY KEY AUTOINCREMENT,package_id INTEGER NOT NULL REFERENCES packages(id),title TEXT NOT NULL,type TEXT NOT NULL CHECK(type IN ('VIDEO','PDF','LINK')),url TEXT NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS learning_progress (user_id INTEGER NOT NULL REFERENCES users(id),resource_id INTEGER NOT NULL REFERENCES learning_resources(id),completed_at INTEGER NOT NULL,PRIMARY KEY(user_id,resource_id))");
        d.execSQL("CREATE TABLE IF NOT EXISTS quiz_questions (id INTEGER PRIMARY KEY AUTOINCREMENT,package_id INTEGER NOT NULL REFERENCES packages(id),question TEXT NOT NULL,option_a TEXT NOT NULL,option_b TEXT NOT NULL,option_c TEXT NOT NULL,option_d TEXT NOT NULL,correct_index INTEGER NOT NULL CHECK(correct_index BETWEEN 0 AND 3))");
        d.execSQL("CREATE TABLE IF NOT EXISTS quiz_attempts (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),question_id INTEGER NOT NULL REFERENCES quiz_questions(id),correct INTEGER NOT NULL,created_at INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),event_key TEXT NOT NULL,message TEXT NOT NULL,read_flag INTEGER NOT NULL DEFAULT 0,created_at INTEGER NOT NULL,UNIQUE(user_id,event_key))");
        d.execSQL("CREATE TABLE IF NOT EXISTS chat_messages (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),role TEXT NOT NULL CHECK(role IN ('USER','ASSISTANT')),message TEXT NOT NULL,created_at INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS user_flags (user_id INTEGER PRIMARY KEY REFERENCES users(id),locked INTEGER NOT NULL DEFAULT 0)");
        d.execSQL("INSERT OR IGNORE INTO vouchers(code,percent,active,expires_at) VALUES('WELCOME10',10,1,4102444800000),('EXAM20',20,1,4102444800000)");
        d.execSQL("INSERT OR IGNORE INTO package_extras(package_id,lecturer,course_code,study_path,trial_url,sale_price) SELECT id,'Giảng viên hỗ trợ','HP-'||id,'Kiến thức nền → thực hành → kiểm tra','',0 FROM packages");
        if(DatabaseUtils.longForQuery(d,"SELECT COUNT(*) FROM learning_resources",null)==0){
            d.execSQL("INSERT INTO learning_resources(package_id,title,type,url) SELECT id,'Bài học mẫu: phương pháp tự học','LINK','https://www.khanacademy.org/' FROM packages LIMIT 4");
            d.execSQL("INSERT INTO learning_resources(package_id,title,type,url) SELECT id,'Tài liệu tham khảo miễn phí','PDF','https://www.openstax.org/' FROM packages LIMIT 4");
        }
        if(DatabaseUtils.longForQuery(d,"SELECT COUNT(*) FROM quiz_questions",null)==0){
            d.execSQL("INSERT INTO quiz_questions(package_id,question,option_a,option_b,option_c,option_d,correct_index) SELECT id,'Khi ôn tập, cách nào giúp ghi nhớ hiệu quả?','Học dồn một lần','Ôn tập cách quãng','Không làm bài tập','Chỉ đọc tiêu đề',1 FROM packages LIMIT 4");
        }
    }
    /** Nâng cấp CSDL từ V3 lên V4 mà không xóa tài khoản, đơn hàng, gói đã tạo. */
    static void migratePackageAttributes(SQLiteDatabase d) {
        String[][] fields={
            {"package_code", "TEXT NOT NULL DEFAULT ''"},
            {"target_students", "TEXT NOT NULL DEFAULT ''"},
            {"level", "TEXT NOT NULL DEFAULT ''"},
            {"lesson_count", "INTEGER NOT NULL DEFAULT 0"},
            {"support_channel", "TEXT NOT NULL DEFAULT ''"},
            {"benefits", "TEXT NOT NULL DEFAULT ''"},
            {"prerequisites", "TEXT NOT NULL DEFAULT ''"}
        };
        for(String[] field:fields){
            boolean found=false;
            try(Cursor c=d.rawQuery("PRAGMA table_info(package_extras)",null)){
                while(c.moveToNext())if(field[0].equals(c.getString(1))){found=true;break;}
            }
            if(!found)d.execSQL("ALTER TABLE package_extras ADD COLUMN "+field[0]+" "+field[1]);
        }
        d.execSQL("INSERT OR IGNORE INTO package_extras(package_id,lecturer,course_code,study_path,trial_url,sale_price) SELECT id,'Giảng viên hỗ trợ','HP-'||id,'Kiến thức nền → thực hành → luyện tập','',0 FROM packages");
        d.execSQL("UPDATE package_extras SET package_code='GOI-' || printf('%03d',package_id) WHERE TRIM(package_code)=''");
        d.execSQL("UPDATE package_extras SET target_students='Sinh viên đại học, cao đẳng' WHERE TRIM(target_students)=''");
        d.execSQL("UPDATE package_extras SET level='Cơ bản đến nâng cao' WHERE TRIM(level)=''");
        d.execSQL("UPDATE package_extras SET lesson_count=12 WHERE lesson_count<0 OR lesson_count=0");
        d.execSQL("UPDATE package_extras SET support_channel='Chat trong ứng dụng, gửi yêu cầu hỗ trợ' WHERE TRIM(support_channel)=''");
        d.execSQL("UPDATE package_extras SET benefits='Bài giảng và tài liệu học tập\nBài tập ôn luyện và hỗ trợ giải đáp\nTheo dõi tiến độ học tập' WHERE TRIM(benefits)=''");
        d.execSQL("UPDATE package_extras SET prerequisites='Không yêu cầu kiến thức đầu vào' WHERE TRIM(prerequisites)=''");
        d.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_package_extras_code ON package_extras(package_code) WHERE package_code<>''");
    }
    boolean isLocked(long userId){
        return DatabaseUtils.longForQuery(db(),"SELECT COUNT(*) FROM user_flags WHERE user_id=? AND locked=1",new String[]{""+userId})>0;
    }
    void setLocked(long userId,boolean locked){ContentValues v=new ContentValues();v.put("user_id",userId);v.put("locked",locked?1:0);db().insertWithOnConflict("user_flags",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    String[] profile(long userId){
        try(Cursor c=db().rawQuery("SELECT university,major,cohort FROM student_profiles WHERE user_id=?",new String[]{""+userId})){
            return c.moveToFirst()?new String[]{c.getString(0),c.getString(1),c.getString(2)}:new String[]{"","",""};
        }
    }
    void saveProfile(long userId,String university,String major,String cohort){
        ContentValues v=new ContentValues();v.put("user_id",userId);v.put("university",university);v.put("major",major);v.put("cohort",cohort);
        db().insertWithOnConflict("student_profiles",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    void addFavorite(long userId,long packageId){db().execSQL("INSERT OR IGNORE INTO favorites(user_id,package_id) VALUES(?,?)",new Object[]{userId,packageId});}
    void addCart(long userId,long packageId){db().execSQL("INSERT OR IGNORE INTO cart_items(user_id,package_id) VALUES(?,?)",new Object[]{userId,packageId});}
    void removeCart(long userId,long packageId){db().delete("cart_items","user_id=? AND package_id=?",new String[]{""+userId,""+packageId});}
    void removeFavorite(long userId,long packageId){db().delete("favorites","user_id=? AND package_id=?",new String[]{""+userId,""+packageId});}
    List<Long> ids(String table,long userId){
        if(!table.equals("favorites")&&!table.equals("cart_items"))throw new IllegalArgumentException("Invalid table");
        List<Long> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT package_id FROM "+table+" WHERE user_id=?",new String[]{""+userId})){while(c.moveToNext())out.add(c.getLong(0));}
        return out;
    }
    int voucher(String code){
        if(code==null||code.trim().isEmpty())return 0;
        try(Cursor c=db().rawQuery("SELECT percent FROM vouchers WHERE code=? AND active=1 AND expires_at>?",new String[]{code.toUpperCase(Locale.ROOT).trim(),""+System.currentTimeMillis()})){
            return c.moveToFirst()?c.getInt(0):0;
        }
    }
    int price(Models.User user, Models.PackageItem p,int discount){
        int pct=discount;
        if(user.email!=null&&user.email.toLowerCase(Locale.ROOT).endsWith(".edu.vn"))pct=Math.max(10,pct);
        int sale=salePrice(p.id);
        return Math.max(0,(int)(Math.min(p.price,sale>0?sale:p.price)*(100L-pct)/100L));
    }
    int salePrice(long packageId){
        return (int)DatabaseUtils.longForQuery(db(),"SELECT COALESCE((SELECT sale_price FROM package_extras WHERE package_id=?),0)",new String[]{""+packageId});
    }
    /** Một đơn hàng cho mỗi gói vì schema orders v1 chỉ hỗ trợ một package_id. */
    int checkout(Models.User user,String code){
        List<Long> ids=ids("cart_items",user.id);
        if(ids.isEmpty())return 0;
        int pct=voucher(code);
        if(code!=null&&!code.trim().isEmpty()&&pct==0)return -1;
        SQLiteDatabase d=db();int done=0;
        d.beginTransaction();
        try{
            for(long id:ids){
                Models.PackageItem p=base.getPackage(id);
                if(p==null||!p.active)continue;
                ContentValues v=new ContentValues();v.put("user_id",user.id);v.put("package_id",id);
                v.put("amount",price(user,p,pct));v.put("status","PENDING");v.put("created_at",System.currentTimeMillis());
                if(d.insertOrThrow("orders",null,v)>0)done++;
            }
            d.delete("cart_items","user_id=?",new String[]{""+user.id});
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return done;
    }
    String details(long packageId){
        Models.PackageItem p=base.getPackage(packageId);
        if(p==null)return "Nội dung đang được cập nhật";
        return "Mã gói: "+p.packageCode+"\n"+
            "Mã học phần: "+p.courseCode+"\n"+
            "Đối tượng: "+p.targetStudents+"\n"+
            "Trình độ: "+p.level+"\n"+
            "Giảng viên: "+p.lecturer+"\n"+
            "Số bài học: "+p.lessonCount+"\n"+
            "Hình thức hỗ trợ: "+p.supportChannel+"\n"+
            "Lộ trình: "+p.studyPath+"\n"+
            "Yêu cầu đầu vào: "+p.prerequisites+"\n"+
            "Học thử: "+(p.trialUrl.isEmpty()?"Chưa có":p.trialUrl);
    }
    String trialUrl(long packageId){
        try(Cursor c=db().rawQuery("SELECT trial_url FROM package_extras WHERE package_id=?",new String[]{""+packageId})){
            return c.moveToFirst()?c.getString(0):"";
        }
    }
    void savePackageExtras(long id,String lecturer,String code,String path,String trial,int sale){
        ContentValues v=new ContentValues();v.put("lecturer",lecturer);v.put("course_code",code);v.put("study_path",path);v.put("trial_url",trial);v.put("sale_price",sale);
        if(db().update("package_extras",v,"package_id=?",new String[]{""+id})==0){
            v.put("package_id",id);db().insertOrThrow("package_extras",null,v);
        }
    }
    boolean hasAccess(long uid,long pid){
        return DatabaseUtils.longForQuery(db(),"SELECT COUNT(*) FROM subscriptions WHERE user_id=? AND package_id=? AND expires_at>?",new String[]{""+uid,""+pid,""+System.currentTimeMillis()})>0;
    }
    void ratePackage(long uid,long pid,int stars,String comment){
        if(!hasAccess(uid,pid)||stars<1||stars>5)return;
        ContentValues v=new ContentValues();v.put("user_id",uid);v.put("package_id",pid);v.put("stars",stars);v.put("comment",comment);
        db().insertWithOnConflict("package_ratings",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    double averageStars(long pid){
        try(Cursor c=db().rawQuery("SELECT AVG(stars) FROM package_ratings WHERE package_id=?",new String[]{""+pid})){
            if(c.moveToFirst()&&!c.isNull(0))return c.getDouble(0);
        }
        return 0;
    }
    String packageRating(long pid){
        try(Cursor c=db().rawQuery("SELECT ROUND(AVG(stars),1),COUNT(*) FROM package_ratings WHERE package_id=?",new String[]{""+pid})){
            if(c.moveToFirst())return c.isNull(0)?"Chưa có đánh giá":"★ "+c.getDouble(0)+"/5 ("+c.getInt(1)+" lượt)";
        }
        return "Chưa có đánh giá";
    }
    List<String[]> resources(long pid){
        List<String[]> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT id,title,type,url FROM learning_resources WHERE package_id=? ORDER BY id",new String[]{""+pid})){
            while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)});
        }return out;
    }
    void addResource(long pid,String title,String type,String url){
        ContentValues v=new ContentValues();v.put("package_id",pid);v.put("title",title);v.put("type",type);v.put("url",url);db().insertOrThrow("learning_resources",null,v);
    }
    boolean isCompleted(long userId,long resourceId){
        return DatabaseUtils.longForQuery(db(),"SELECT COUNT(*) FROM learning_progress WHERE user_id=? AND resource_id=?",new String[]{""+userId,""+resourceId})>0;
    }
    void complete(long userId,long resourceId){
        ContentValues v=new ContentValues();v.put("user_id",userId);v.put("resource_id",resourceId);v.put("completed_at",System.currentTimeMillis());
        db().insertWithOnConflict("learning_progress",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    int progress(long userId,long pid){
        long total=DatabaseUtils.longForQuery(db(),"SELECT COUNT(*) FROM learning_resources WHERE package_id=?",new String[]{""+pid});
        if(total==0)return 0;
        long done=DatabaseUtils.longForQuery(db(),"SELECT COUNT(*) FROM learning_progress g JOIN learning_resources r ON r.id=g.resource_id WHERE g.user_id=? AND r.package_id=?",new String[]{""+userId,""+pid});
        return (int)(100L*done/total);
    }
    List<String[]> questions(long pid){
        List<String[]> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT id,question,option_a,option_b,option_c,option_d,correct_index FROM quiz_questions WHERE package_id=? ORDER BY id",new String[]{""+pid})){
            while(c.moveToNext()){String[] row=new String[7];for(int i=0;i<7;i++)row[i]=c.getString(i);out.add(row);}
        }return out;
    }
    void addQuestion(long pid,String q,String a,String b,String c,String d,int correct){
        ContentValues v=new ContentValues();v.put("package_id",pid);v.put("question",q);v.put("option_a",a);v.put("option_b",b);v.put("option_c",c);v.put("option_d",d);v.put("correct_index",correct);
        db().insertOrThrow("quiz_questions",null,v);
    }
    void quizAnswer(long uid,long qid,boolean correct){
        ContentValues v=new ContentValues();v.put("user_id",uid);v.put("question_id",qid);v.put("correct",correct?1:0);v.put("created_at",System.currentTimeMillis());db().insertOrThrow("quiz_attempts",null,v);
    }
    void notifyUser(long uid,String key,String message){
        ContentValues v=new ContentValues();v.put("user_id",uid);v.put("event_key",key);v.put("message",message);v.put("created_at",System.currentTimeMillis());
        db().insertWithOnConflict("notifications",null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    List<String[]> notifications(long uid){
        List<String[]> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT id,message,read_flag FROM notifications WHERE user_id=? ORDER BY id DESC",new String[]{""+uid})){
            while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});
        }return out;
    }
    void markRead(long uid,long notifId){ContentValues v=new ContentValues();v.put("read_flag",1);db().update("notifications",v,"id=? AND user_id=?",new String[]{""+notifId,""+uid});}
    void chat(long uid,String role,String text){
        ContentValues v=new ContentValues();v.put("user_id",uid);v.put("role",role);v.put("message",text);v.put("created_at",System.currentTimeMillis());db().insertOrThrow("chat_messages",null,v);
    }
    List<String[]> chatHistory(long uid){
        List<String[]> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT role,message FROM chat_messages WHERE user_id=? ORDER BY id DESC LIMIT 60",new String[]{""+uid})){while(c.moveToNext())out.add(0,new String[]{c.getString(0),c.getString(1)});}
        return out;
    }
    List<String[]> users(){
        List<String[]> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT u.id,u.name,u.email,u.role,COALESCE(f.locked,0) FROM users u LEFT JOIN user_flags f ON f.user_id=u.id ORDER BY u.id",null)){
            while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)});
        }return out;
    }
    void saveVoucher(String code,int pct,long expiresAt){
        ContentValues v=new ContentValues();v.put("code",code.toUpperCase(Locale.ROOT));v.put("percent",pct);v.put("expires_at",expiresAt);v.put("active",1);
        db().insertWithOnConflict("vouchers",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    List<String> vouchers(){List<String> out=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT code,percent,active FROM vouchers ORDER BY code",null)){while(c.moveToNext())out.add(c.getString(0)+"  -"+c.getInt(1)+"%  "+(c.getInt(2)==1?"Đang hoạt động":"Đã ẩn"));}return out;}
}
