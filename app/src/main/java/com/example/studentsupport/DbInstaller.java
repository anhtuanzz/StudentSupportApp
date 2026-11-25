package com.example.studentsupport;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

/** Copy CSDL đã thiết kế trong DB Browser vào bộ nhớ riêng của ứng dụng ở lần chạy đầu. */
final class DbInstaller {
    static void ensureInstalled(Context context) {
        File target = context.getDatabasePath(DbHelper.DB_NAME);
        if (target.exists()) return; // Không ghi đè dữ liệu thực tế của người dùng.
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Không thể tạo thư mục databases");
        }
        try (InputStream in = context.getAssets().open(DbHelper.DB_NAME);
             FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
        } catch (IOException e) {
            if (target.exists()) target.delete();
            throw new IllegalStateException("Không thể sao chép file SQLite từ assets", e);
        }
    }
}
