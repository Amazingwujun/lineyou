package com.jun.lineyou.entity;

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

    public String getUsername() {
        return username.get();
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
}
