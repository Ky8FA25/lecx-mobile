package com.example.lecx_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
    }

    public static void saveSession(Context ctx, int userId, String email, boolean remember) {
        sp(ctx).edit()
                .putInt(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_EMAIL, email)
                .putBoolean(Constants.KEY_REMEMBER, remember)
//                .putBoolean(Constants.KEY_IS_ADMIN, isAdmin)
                .apply();
    }

    public static void clearSession(Context ctx) {
        sp(ctx).edit().clear().apply();
    }

    public static int getUserId(Context ctx) {
        return sp(ctx).getInt(Constants.KEY_USER_ID, -1);
    }

    public static String getEmail(Context ctx) {
        return sp(ctx).getString(Constants.KEY_EMAIL, null);
    }

    public static String getAvatar(Context ctx) {
        return sp(ctx).getString(Constants.KEY_AVATAR, null);
    }

    public static boolean isRemember(Context ctx) {
        return sp(ctx).getBoolean(Constants.KEY_REMEMBER, false);
    }

//    public static boolean isAdmin(Context ctx) {
//        return sp(ctx).getBoolean(Constants.KEY_IS_ADMIN, false);
//    }
}
