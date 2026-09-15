package com.sky;

import com.sky.utils.JwtUtil;
import java.util.HashMap;
import java.util.Map;

public class TestToken {
    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L); 
        String token = JwtUtil.createJWT("itheima", 7200000, claims);
        
        System.out.println("================================");
        System.out.println("生成的用户端 Token 如下：");
        System.out.println(token);
        System.out.println("================================");
    }
}