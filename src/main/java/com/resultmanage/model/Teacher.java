package com.resultmanage.model;

import com.resultmanage.util.JsonUtil;

public class Teacher {
     private final int id;
    private final String name;
    private final String email;
    private final String passwordHash;
    public Teacher(int id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
        public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"name\":" + JsonUtil.str(name) + ","
                + "\"email\":" + JsonUtil.str(email)
                + "}";
    }
}
