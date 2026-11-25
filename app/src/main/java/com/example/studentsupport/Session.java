package com.example.studentsupport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/** Lưu ID người dùng đăng nhập cục bộ, các bảng vẫn kiểm tra quyền tại lớp dữ liệu. */
final class Session {
    static long userId(Context c) {
        return c.getSharedPreferences("auth", Context.MODE_PRIVATE).getLong("user_id", -1L);
    }
    static void login(Context c, long userId) {
        c.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().putLong("user_id", userId).apply();
    }
    static void logout(Activity a) {
        a.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(a, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        a.startActivity(intent);
        a.finish();
    }
    static Models.User require(Activity a, DbHelper db) {
        Models.User user = db.getUser(userId(a));
        if (user == null || new ExtraStore(db).isLocked(user.id)) {
            logout(a);
            return null;
        }
        return user;
    }
}
