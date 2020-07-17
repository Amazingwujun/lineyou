package com.jun.lineyou.entity;

import com.alibaba.fastjson.JSONObject;
import javafx.beans.property.SimpleStringProperty;

/**
 * 登入用户
 *
 * @author Jun
 * @date 2020-07-01 13:26
 */
public class User {

    private SimpleStringProperty username = new SimpleStringProperty();

    private SimpleStringProperty password = new SimpleStringProperty();

    private String token;

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", username.getValue());
        jsonObject.put("password", password.getValue());
        return jsonObject.toString();
    }
}
