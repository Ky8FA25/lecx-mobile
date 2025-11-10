package com.example.lecx_mobile.models;

public class Account {
    public int id;
    public String fullname;
    public String username;
    public String password;
    public String avatar;
    public String email;
    public boolean isEmailConfirmed;

    public Account() {} // Firebase cần constructor rỗng

    public Account(String fullname, String username, String password, String avatar, String email, boolean isEmailConfirmed) {
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.email = email;
        this.isEmailConfirmed = isEmailConfirmed;
    }
}
