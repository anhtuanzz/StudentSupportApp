package com.example.studentsupport;

/** Các lớp dữ liệu dùng bởi ListView và Activity. */
final class Models {
    static class User {
        long id;
        String name, email, studentCode, phone, role;
    }
    static class PackageItem {
        long id;
        String name, category, description;
        // Thuộc tính mở rộng của gói hỗ trợ, lưu tại package_extras.
        String packageCode, lecturer, courseCode, studyPath, trialUrl;
        String targetStudents, level, supportChannel, benefits, prerequisites;
        int price, durationDays, lessonCount, salePrice;
        boolean active;
    }
    static class OrderItem {
        long id, userId, packageId, createdAt;
        String packageName, userName, status;
        int amount;
    }
    static class SubscriptionItem {
        long id, packageId, expiresAt;
        String packageName;
        int durationDays;
    }
    static class TicketItem {
        long id, userId, createdAt;
        String userName, subject, message, reply, status;
        int rating;
    }
}
