package com.jun.lineyou;


import com.alibaba.fastjson.JSON;
import com.jun.lineyou.entity.User;

class LineyouApplicationTests {

    public static void main(String[] args) {
        User user = new User();
        user.setPassword("nani");
        System.out.println(JSON.toJSONString(user));
    }
}
