package com.clms.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.utils.WeChatMessageCryptoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信小程序消息推送控制器。
 *
 * 配置项与微信后台保持一致：
 * URL: https://liuservice.online/wechat
 * Token: 019d80619265772fa2fda9f69ad1245b
 * EncodingAESKey: PBFdTA2lubIBmRPXtwxgIxiHCgbv0yKgcxZx25yKhSW
 */
@Slf4j
@RestController
@RequestMapping("/wechat")
@Tag(name = "微信订阅消息推送接口")
@Validated
public class WeChatSubscriptionMessageController {

    @Value("${wechat.push.token:}")
    private String token;

    @Value("${wechat.push.encoding-aes-key:}")
    private String encodingAesKey;

    /**
     * 微信后台配置URL时的GET校验。
     */
    @SaIgnore
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "微信推送URL校验")
    public String verifyUrl(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        boolean valid = WeChatMessageCryptoUtil.verifyUrlSignature(signature, token, timestamp, nonce);
        if (!valid) {
            log.warn("微信URL校验失败: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
            return "";
        }
        return echostr;
    }
}
