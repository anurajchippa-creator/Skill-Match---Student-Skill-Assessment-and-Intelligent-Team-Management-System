package com.skillmatch.ui;

public class Session {
    private static Long userId;
    private static String fullName;
    private static String role;

    public static void setUser(Long id, String name, String r) {
        userId = id;
        fullName = name;
        role = r;
    }

    public static Long getUserId() { return userId; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }

    public static void clear() {
        userId = null;
        fullName = null;
        role = null;
    }
}
