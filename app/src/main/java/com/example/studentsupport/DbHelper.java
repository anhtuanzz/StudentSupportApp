package com.example.studentsupport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Toàn bộ truy vấn SQLite; sử dụng Cursor, ContentValues, SQLiteOpenHelper như bài 9-10. */
final class DbHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "student_support.db";
    private static final int DB_VERSION = 4;
    DbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        // Phương án dự phòng khi tạo DB trống (app thông thường chép DB đã thiết kế từ assets).
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, student_code TEXT UNIQUE, phone TEXT NOT NULL DEFAULT '', role TEXT NOT NULL CHECK(role IN ('ADMIN','STUDENT')), password_hash TEXT NOT NULL, password_salt TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE packages (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT NOT NULL, description TEXT NOT NULL, price INTEGER NOT NULL CHECK(price>=0), duration_days INTEGER NOT NULL CHECK(duration_days>0), is_active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), package_id INTEGER NOT NULL REFERENCES packages(id), amount INTEGER NOT NULL, status TEXT NOT NULL CHECK(status IN ('PENDING','PAID','COMPLETED','CANCELLED')), created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE payments (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL UNIQUE REFERENCES orders(id), amount INTEGER NOT NULL, method TEXT NOT NULL, status TEXT NOT NULL, confirmed_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE subscriptions (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), package_id INTEGER NOT NULL REFERENCES packages(id), started_at INTEGER NOT NULL, expires_at INTEGER NOT NULL, UNIQUE(user_id, package_id))");
        db.execSQL("CREATE TABLE tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), subject TEXT NOT NULL, message TEXT NOT NULL, reply TEXT NOT NULL DEFAULT '', status TEXT NOT NULL CHECK(status IN ('OPEN','RESOLVED')), created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE ratings (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), ticket_id INTEGER NOT NULL UNIQUE REFERENCES tickets(id), stars INTEGER NOT NULL CHECK(stars BETWEEN 1 AND 5), comment TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_orders_user ON orders(user_id)");
        db.execSQL("CREATE INDEX idx_tickets_user ON tickets(user_id)");
        addSeedUser(db,"Quản trị viên","admin@demo.local",null,"","ADMIN","Admin@123");
        addSeedUser(db,"Sinh viên Demo","sv01@demo.local","SV001","0900000001","STUDENT","Student@123");
        db.execSQL("INSERT INTO packages(name,category,description,price,duration_days) VALUES ('Gói Nền tảng đại học','Đại cương','Bổ sung kiến thức cơ bản và củng cố nền tảng các môn học.',199000,30),('Gói Luyện thi toàn diện','Luyện thi','Ôn tập và luyện đề thi có giải đáp.',299000,60),('Gói Kỹ năng CNTT','CNTT','Ứng dụng kỹ năng số vào học tập và làm việc.',349000,90),('Gói Tiếng Anh giao tiếp','Ngoại ngữ','Tăng kỹ năng nghe nói và phản xạ.',249000,60)");
        db.execSQL("INSERT INTO packages(name,category,description,price,duration_days) VALUES ('Luyện thi TOEIC','Ngoại ngữ','Bộ đề luyện TOEIC, từ vựng và hỗ trợ giải đáp.',199000,30),('Ôn tập Toán cao cấp','Đại cương','Tài liệu, bài tập và hướng dẫn giải chi tiết.',99000,45),('Tin học văn phòng','Tin học','Học Word, Excel, PowerPoint từ cơ bản.',149000,60),('Kỹ năng làm CV','Kỹ năng','Mẫu CV và góp ý hồ sơ xin thực tập.',79000,30)");
        ExtraStore.createTables(db);
        ExtraStore.migratePackageAttributes(db);
    }
    private void addSeedUser(SQLiteDatabase db, String name, String email, String code, String phone, String role, String pass) {
        String salt=PasswordUtil.newSalt();
        ContentValues v=new ContentValues();
        v.put("name",name);v.put("email",email);v.put("student_code",code);v.put("phone",phone);
        v.put("role",role);v.put("password_salt",salt);v.put("password_hash",PasswordUtil.hash(pass,salt));v.put("created_at",System.currentTimeMillis());
        db.insertOrThrow("users",null,v);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) ExtraStore.createTables(db);
        if (oldVersion < 4) ExtraStore.migratePackageAttributes(db);
        // Giữ toàn bộ tài khoản, đơn hàng và gói khi nâng cấp V3 -> V4.
    }

    private Models.User readUser(Cursor c) {
        Models.User u=new Models.User();
        u.id=c.getLong(c.getColumnIndexOrThrow("id"));
        u.name=c.getString(c.getColumnIndexOrThrow("name"));
        u.email=c.getString(c.getColumnIndexOrThrow("email"));
        u.studentCode=c.getString(c.getColumnIndexOrThrow("student_code"));
        u.phone=c.getString(c.getColumnIndexOrThrow("phone"));
        u.role=c.getString(c.getColumnIndexOrThrow("role"));
        return u;
    }
    Models.User getUser(long id) {
        try (Cursor c=getReadableDatabase().rawQuery("SELECT * FROM users WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? readUser(c) : null;
        }
    }
    Models.User authenticate(String email,String password) {
        String q=email.trim().toLowerCase(Locale.ROOT);
        try (Cursor c=getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=?",new String[]{q})) {
            if (!c.moveToFirst()) return null;
            String salt=c.getString(c.getColumnIndexOrThrow("password_salt"));
            String hash=c.getString(c.getColumnIndexOrThrow("password_hash"));
            return PasswordUtil.hash(password,salt).equals(hash) && !new ExtraStore(this).isLocked(c.getLong(c.getColumnIndexOrThrow("id"))) ? readUser(c) : null;
        }
    }
    boolean register(String name,String email,String code,String phone,String pass) {
        if (name.isEmpty() || email.isEmpty() || code.isEmpty() || pass.length()<8) return false;
        ContentValues v=new ContentValues();
        String salt=PasswordUtil.newSalt();
        v.put("name",name);v.put("email",email.toLowerCase(Locale.ROOT));
        v.put("student_code",code.toUpperCase(Locale.ROOT));v.put("phone",phone);
        v.put("role","STUDENT");v.put("password_salt",salt);v.put("password_hash",PasswordUtil.hash(pass,salt));
        v.put("created_at",System.currentTimeMillis());
        return getWritableDatabase().insert("users",null,v)!=-1;
    }
    boolean resetPassword(String email,String code,String pass) {
        if (pass.length()<8 || code.isEmpty()) return false;
        String salt=PasswordUtil.newSalt();
        ContentValues v=new ContentValues();v.put("password_salt",salt);v.put("password_hash",PasswordUtil.hash(pass,salt));
        return getWritableDatabase().update("users",v,"email=? AND student_code=? AND role='STUDENT'",
            new String[]{email.toLowerCase(Locale.ROOT),code.toUpperCase(Locale.ROOT)})==1;
    }
    boolean updateProfile(long userId,String name,String phone) {
        if (name.isEmpty()) return false;
        ContentValues v=new ContentValues();v.put("name",name);v.put("phone",phone);
        return getWritableDatabase().update("users",v,"id=?",new String[]{String.valueOf(userId)})==1;
    }

    private static final String PACKAGE_COLUMNS =
        "SELECT p.*, COALESCE(x.lecturer,'') AS lecturer, COALESCE(x.course_code,'') AS course_code, "+
        "COALESCE(x.study_path,'') AS study_path, COALESCE(x.trial_url,'') AS trial_url, "+
        "COALESCE(x.sale_price,0) AS sale_price, COALESCE(x.package_code,'') AS package_code, "+
        "COALESCE(x.target_students,'') AS target_students, COALESCE(x.level,'') AS level, "+
        "COALESCE(x.lesson_count,0) AS lesson_count, COALESCE(x.support_channel,'') AS support_channel, "+
        "COALESCE(x.benefits,'') AS benefits, COALESCE(x.prerequisites,'') AS prerequisites "+
        "FROM packages p LEFT JOIN package_extras x ON x.package_id=p.id ";

    private Models.PackageItem readPackage(Cursor c) {
        Models.PackageItem p=new Models.PackageItem();
        p.id=c.getLong(c.getColumnIndexOrThrow("id"));
        p.name=c.getString(c.getColumnIndexOrThrow("name"));
        p.category=c.getString(c.getColumnIndexOrThrow("category"));
        p.description=c.getString(c.getColumnIndexOrThrow("description"));
        p.price=c.getInt(c.getColumnIndexOrThrow("price"));
        p.durationDays=c.getInt(c.getColumnIndexOrThrow("duration_days"));
        p.active=c.getInt(c.getColumnIndexOrThrow("is_active"))==1;
        p.packageCode=c.getString(c.getColumnIndexOrThrow("package_code"));
        p.lecturer=c.getString(c.getColumnIndexOrThrow("lecturer"));
        p.courseCode=c.getString(c.getColumnIndexOrThrow("course_code"));
        p.studyPath=c.getString(c.getColumnIndexOrThrow("study_path"));
        p.trialUrl=c.getString(c.getColumnIndexOrThrow("trial_url"));
        p.salePrice=c.getInt(c.getColumnIndexOrThrow("sale_price"));
        p.targetStudents=c.getString(c.getColumnIndexOrThrow("target_students"));
        p.level=c.getString(c.getColumnIndexOrThrow("level"));
        p.lessonCount=c.getInt(c.getColumnIndexOrThrow("lesson_count"));
        p.supportChannel=c.getString(c.getColumnIndexOrThrow("support_channel"));
        p.benefits=c.getString(c.getColumnIndexOrThrow("benefits"));
        p.prerequisites=c.getString(c.getColumnIndexOrThrow("prerequisites"));
        return p;
    }
    Models.PackageItem getPackage(long id) {
        try(Cursor c=getReadableDatabase().rawQuery(PACKAGE_COLUMNS+"WHERE p.id=?",new String[]{String.valueOf(id)})) {
            return c.moveToFirst()?readPackage(c):null;
        }
    }
    List<String> categories() {
        List<String> all=new ArrayList<>();all.add("Tất cả danh mục");
        try(Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT category FROM packages WHERE is_active=1 ORDER BY category",null)) {
            while(c.moveToNext()) all.add(c.getString(0));
        }
        return all;
    }
    List<Models.PackageItem> packages(String keyword,String category) {
        List<Models.PackageItem> out=new ArrayList<>();
        String sql=PACKAGE_COLUMNS+"WHERE p.is_active=1 AND (p.name LIKE ? OR p.description LIKE ? OR x.lecturer LIKE ? OR x.course_code LIKE ? OR x.package_code LIKE ? OR x.target_students LIKE ?)";
        String key="%"+keyword+"%";
        String[] args;
        if (category.equals("Tất cả danh mục")) args=new String[]{key,key,key,key,key,key};
        else {sql+=" AND p.category=?";args=new String[]{key,key,key,key,key,key,category};}
        sql+=" ORDER BY p.id DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,args)) {
            while(c.moveToNext()) out.add(readPackage(c));
        }
        return out;
    }
    /** Lưu gói + thuộc tính mở rộng cùng transaction, tránh lưu nửa chừng. */
    boolean savePackage(Models.User actor,Models.PackageItem p) {
        if(actor==null || !"ADMIN".equals(actor.role) || p==null || p.name.isEmpty() || p.category.isEmpty()
            || p.description.isEmpty() || p.packageCode.isEmpty() || p.price<0 || p.durationDays<1 || p.durationDays>3650
            || p.salePrice<0 || p.salePrice>p.price || p.lessonCount<0) return false;
        SQLiteDatabase d=getWritableDatabase();
        // Kiểm tra mã gói duy nhất, kể cả khi thêm mới.
        try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM package_extras WHERE package_code=? AND package_id<>?",
                new String[]{p.packageCode,""+p.id})){
            if(c.moveToFirst()&&c.getInt(0)>0)return false;
        }
        d.beginTransaction();
        try{
            ContentValues v=new ContentValues();
            v.put("name",p.name);v.put("category",p.category);v.put("description",p.description);
            v.put("price",p.price);v.put("duration_days",p.durationDays);
            long pid=p.id;
            if(pid<=0){pid=d.insertOrThrow("packages",null,v);}
            else if(d.update("packages",v,"id=? AND is_active=1",new String[]{""+pid})!=1)return false;
            ContentValues ext=new ContentValues();
            ext.put("package_id",pid);ext.put("lecturer",p.lecturer);ext.put("course_code",p.courseCode);
            ext.put("study_path",p.studyPath);ext.put("trial_url",p.trialUrl);ext.put("sale_price",p.salePrice);
            ext.put("package_code",p.packageCode);ext.put("target_students",p.targetStudents);
            ext.put("level",p.level);ext.put("lesson_count",p.lessonCount);
            ext.put("support_channel",p.supportChannel);ext.put("benefits",p.benefits);
            ext.put("prerequisites",p.prerequisites);
            // UPDATE trước để không xóa record phụ thuộc (khác CONFLICT_REPLACE).
            if(d.update("package_extras",ext,"package_id=?",new String[]{""+pid})==0)
                d.insertOrThrow("package_extras",null,ext);
            d.setTransactionSuccessful();
            return true;
        }catch(android.database.SQLException ex){return false;}
        finally{d.endTransaction();}
    }
    boolean deletePackage(Models.User actor,long id) {
        if(actor==null || !"ADMIN".equals(actor.role))return false;
        ContentValues v=new ContentValues();v.put("is_active",0); // Xóa mềm, giữ hóa đơn cũ.
        return getWritableDatabase().update("packages",v,"id=? AND is_active=1",new String[]{String.valueOf(id)})==1;
    }

    long createOrder(long userId,long packageId) {
        Models.User actor=getUser(userId);
        Models.PackageItem p=getPackage(packageId);
        if(actor==null || !"STUDENT".equals(actor.role) || p==null || !p.active) return -1;
        ContentValues v=new ContentValues();v.put("user_id",userId);v.put("package_id",packageId);
        v.put("amount",new ExtraStore(this).price(actor,p,0));v.put("status","PENDING");v.put("created_at",System.currentTimeMillis());
        return getWritableDatabase().insert("orders",null,v);
    }
    private Models.OrderItem readOrder(Cursor c) {
        Models.OrderItem o=new Models.OrderItem();
        o.id=c.getLong(c.getColumnIndexOrThrow("id"));
        o.userId=c.getLong(c.getColumnIndexOrThrow("user_id"));
        o.packageId=c.getLong(c.getColumnIndexOrThrow("package_id"));
        o.amount=c.getInt(c.getColumnIndexOrThrow("amount"));
        o.createdAt=c.getLong(c.getColumnIndexOrThrow("created_at"));
        o.status=c.getString(c.getColumnIndexOrThrow("status"));
        o.packageName=c.getString(c.getColumnIndexOrThrow("package_name"));
        o.userName=c.getString(c.getColumnIndexOrThrow("user_name"));
        return o;
    }
    private static final String ORDER_SQL="SELECT o.*,p.name AS package_name,u.name AS user_name FROM orders o JOIN packages p ON p.id=o.package_id JOIN users u ON u.id=o.user_id";
    Models.OrderItem getOrder(long id) {
        try(Cursor c=getReadableDatabase().rawQuery(ORDER_SQL+" WHERE o.id=?",new String[]{String.valueOf(id)})){
            return c.moveToFirst()?readOrder(c):null;
        }
    }
    List<Models.OrderItem> orders(Models.User actor) {
        List<Models.OrderItem> out=new ArrayList<>();
        boolean admin="ADMIN".equals(actor.role);
        String sql=ORDER_SQL+(admin?"":" WHERE o.user_id=?")+" ORDER BY o.id DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,admin?null:new String[]{String.valueOf(actor.id)})){
            while(c.moveToNext())out.add(readOrder(c));
        }
        return out;
    }
    boolean cancelOrder(long userId,long orderId) {
        ContentValues v=new ContentValues();v.put("status","CANCELLED");
        return getWritableDatabase().update("orders",v,"id=? AND user_id=? AND status='PENDING'",
                new String[]{String.valueOf(orderId),String.valueOf(userId)})==1;
    }
    boolean completeOrder(Models.User admin,long id) {
        if(admin==null || !"ADMIN".equals(admin.role))return false;
        ContentValues v=new ContentValues();v.put("status","COMPLETED");
        return getWritableDatabase().update("orders",v,"id=? AND status='PAID'",new String[]{String.valueOf(id)})==1;
    }
    boolean payDemo(long userId,long orderId) {
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try(Cursor c=db.rawQuery("SELECT o.amount,o.package_id,p.duration_days FROM orders o JOIN packages p ON o.package_id=p.id WHERE o.id=? AND o.user_id=? AND o.status='PENDING'",new String[]{String.valueOf(orderId),String.valueOf(userId)})) {
            if(!c.moveToFirst())return false;
            int amount=c.getInt(0);
            long packageId=c.getLong(1);
            int days=c.getInt(2);
            long now=System.currentTimeMillis();
            ContentValues payment=new ContentValues();payment.put("order_id",orderId);
            payment.put("amount",amount);payment.put("method","QR_DEMO");payment.put("status","CONFIRMED_DEMO");payment.put("confirmed_at",now);
            if(db.insert("payments",null,payment)<=0)return false;
            ContentValues paid=new ContentValues();paid.put("status","PAID");
            if(db.update("orders",paid,"id=? AND status='PENDING'",new String[]{String.valueOf(orderId)})!=1)return false;
            long expiry=now;
            try(Cursor s=db.rawQuery("SELECT expires_at FROM subscriptions WHERE user_id=? AND package_id=?",new String[]{String.valueOf(userId),String.valueOf(packageId)})) {
                if(s.moveToFirst())expiry=Math.max(now,s.getLong(0));
            }
            expiry+=days*86_400_000L;
            ContentValues sub=new ContentValues();sub.put("expires_at",expiry);
            int rows=db.update("subscriptions",sub,"user_id=? AND package_id=?",new String[]{String.valueOf(userId),String.valueOf(packageId)});
            if(rows==0) {
                sub.put("user_id",userId);sub.put("package_id",packageId);sub.put("started_at",now);
                if(db.insert("subscriptions",null,sub)<=0)return false;
            }
            db.setTransactionSuccessful();
            return true;
        } finally { db.endTransaction(); }
    }

    List<Models.SubscriptionItem> subscriptions(long userId) {
        List<Models.SubscriptionItem> out=new ArrayList<>();
        String sql="SELECT s.id,s.package_id,s.expires_at,p.name,p.duration_days FROM subscriptions s JOIN packages p ON p.id=s.package_id WHERE s.user_id=? ORDER BY s.expires_at DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(userId)})) {
            while(c.moveToNext()){
                Models.SubscriptionItem s=new Models.SubscriptionItem();
                s.id=c.getLong(0);s.packageId=c.getLong(1);s.expiresAt=c.getLong(2);s.packageName=c.getString(3);s.durationDays=c.getInt(4);
                out.add(s);
            }
        }
        return out;
    }
    long createTicket(long userId,String subject,String message) {
        if(subject.isEmpty() || message.isEmpty())return -1;
        Models.User u=getUser(userId);if(u==null || !"STUDENT".equals(u.role))return -1;
        long now=System.currentTimeMillis();
        ContentValues v=new ContentValues();v.put("user_id",userId);v.put("subject",subject);v.put("message",message);
        v.put("status","OPEN");v.put("created_at",now);v.put("updated_at",now);
        return getWritableDatabase().insert("tickets",null,v);
    }
    private Models.TicketItem readTicket(Cursor c) {
        Models.TicketItem t=new Models.TicketItem();
        t.id=c.getLong(c.getColumnIndexOrThrow("id"));
        t.userId=c.getLong(c.getColumnIndexOrThrow("user_id"));
        t.userName=c.getString(c.getColumnIndexOrThrow("user_name"));
        t.subject=c.getString(c.getColumnIndexOrThrow("subject"));
        t.message=c.getString(c.getColumnIndexOrThrow("message"));
        t.reply=c.getString(c.getColumnIndexOrThrow("reply"));
        t.status=c.getString(c.getColumnIndexOrThrow("status"));
        t.createdAt=c.getLong(c.getColumnIndexOrThrow("created_at"));
        t.rating=c.getInt(c.getColumnIndexOrThrow("rating"));
        return t;
    }
    private static final String TICKET_SQL="SELECT t.*,u.name AS user_name,COALESCE(r.stars,0) AS rating FROM tickets t JOIN users u ON u.id=t.user_id LEFT JOIN ratings r ON r.ticket_id=t.id";
    List<Models.TicketItem> tickets(Models.User actor) {
        List<Models.TicketItem> out=new ArrayList<>();
        boolean admin="ADMIN".equals(actor.role);
        try(Cursor c=getReadableDatabase().rawQuery(TICKET_SQL+(admin?"":" WHERE t.user_id=?")+" ORDER BY t.id DESC",admin?null:new String[]{String.valueOf(actor.id)})) {
            while(c.moveToNext())out.add(readTicket(c));
        }
        return out;
    }
    boolean resolveTicket(Models.User admin,long ticketId,String reply) {
        if(admin==null || !"ADMIN".equals(admin.role) || reply.isEmpty())return false;
        ContentValues v=new ContentValues();v.put("reply",reply);v.put("status","RESOLVED");v.put("updated_at",System.currentTimeMillis());
        return getWritableDatabase().update("tickets",v,"id=?",new String[]{String.valueOf(ticketId)})==1;
    }
    boolean rateTicket(long userId,long ticketId,int stars,String comment) {
        if(stars<1 || stars>5)return false;
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id FROM tickets WHERE id=? AND user_id=? AND status='RESOLVED'",new String[]{String.valueOf(ticketId),String.valueOf(userId)})) {
            if(!c.moveToFirst())return false;
        }
        ContentValues v=new ContentValues();v.put("user_id",userId);v.put("ticket_id",ticketId);v.put("stars",stars);v.put("comment",comment);v.put("created_at",System.currentTimeMillis());
        return getWritableDatabase().insert("ratings",null,v)>0; // UNIQUE(ticket_id) ngăn đánh giá hai lần
    }
    long count(String sql) {return DatabaseUtils.longForQuery(getReadableDatabase(),sql,null);}
    String statsText() {
        long total=count("SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='CONFIRMED_DEMO'");
        long paid=count("SELECT COUNT(*) FROM payments WHERE status='CONFIRMED_DEMO'");
        long pending=count("SELECT COUNT(*) FROM orders WHERE status='PENDING'");
        long members=count("SELECT COUNT(*) FROM users WHERE role='STUDENT'");
        long active=DatabaseUtils.longForQuery(getReadableDatabase(),"SELECT COUNT(*) FROM subscriptions WHERE expires_at>?",new String[]{String.valueOf(System.currentTimeMillis())});
        long tickets=count("SELECT COUNT(*) FROM tickets WHERE status='OPEN'");
        String avg="Chưa có";
        try(Cursor c=getReadableDatabase().rawQuery("SELECT AVG(stars) FROM ratings",null)) {
            if(c.moveToFirst()&&!c.isNull(0))avg=String.format(Locale.US,"%.1f/5",c.getDouble(0));
        }
        return "Doanh thu mô phỏng: "+Ui.money(total)+"\nThanh toán thành công: "+paid+
            "\nĐơn chờ thanh toán: "+pending+"\nSinh viên: "+members+
            "\nGói đang hiệu lực: "+active+"\nYêu cầu chưa xử lý: "+tickets+"\nĐánh giá trung bình: "+avg;
    }
    String revenueByPackage() {
        StringBuilder b=new StringBuilder();
        String sql="SELECT p.name,COUNT(pm.id),SUM(pm.amount) FROM payments pm JOIN orders o ON o.id=pm.order_id JOIN packages p ON p.id=o.package_id WHERE pm.status='CONFIRMED_DEMO' GROUP BY p.id ORDER BY SUM(pm.amount) DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,null)) {
            while(c.moveToNext())b.append("• ").append(c.getString(0)).append("\n  ").append(c.getLong(1)).append(" lượt — ").append(Ui.money(c.getLong(2))).append("\n\n");
        }
        return b.length()==0?"Chưa có đơn thanh toán.":b.toString();
    }
}
