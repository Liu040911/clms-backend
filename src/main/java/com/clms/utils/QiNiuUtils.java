package com.clms.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

@Service
public class QiNiuUtils {
    @Value("${qiniu.sms.access-key:}")
    private String ACCESS_KEY;

    @Value("${qiniu.sms.secret-key:}")
    private String SECRET_KEY;

    private static final String BUCKET_NAME = "clms";


    public String getUploadToken(String key) {
        System.out.println("ACCESS_KEY=" + ACCESS_KEY);
        System.out.println("SECRET_KEY=" + SECRET_KEY);
        try {
            Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
            StringMap putPolicy = new StringMap();
            putPolicy.put("scope", BUCKET_NAME);
            putPolicy.put("mimeLimit", "image/*;video/*;audio/*;application/pdf;application/octet-stream");
            putPolicy.put("callbackBodyType", "application/json");
            
            // 只有在key不为空时才设置saveKey策略
            if (key != null && !key.trim().isEmpty()) {
                putPolicy.put("saveKey", key);
                String token = auth.uploadToken(BUCKET_NAME, key, 3600, putPolicy);
                return token;
            } else {
                // 如果key为空，生成一个通用的上传token
                String token = auth.uploadToken(BUCKET_NAME, null, 3600, putPolicy);
                return token;
            }
        } catch (Exception e) {
            throw new RuntimeException("生成上传token失败: " + e.getMessage(), e);
        }
    }
}
