package com.example.lecx_mobile.utils;

import android.util.Patterns;

public final class Validator {
    private Validator() {}

    public static boolean isNonEmpty(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String pass) {
        return pass != null && pass.length() >= 6;
    }

    public static boolean passwordsMatch(String pass, String confirm) {
        return pass != null && pass.equals(confirm);
    }
}
