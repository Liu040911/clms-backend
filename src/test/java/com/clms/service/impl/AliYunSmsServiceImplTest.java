package com.clms.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AliYunSmsServiceImpl 测试
 *
 * 外部接口（阿里云客户端）使用 Mock，服务类内部逻辑使用真实实现。
 */
@ExtendWith(MockitoExtension.class)
class AliYunSmsServiceImplTest {

    private AliYunSmsServiceImpl smsService;

    @Mock
    private Client mockClient;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() throws Exception {
        smsService = spy(new AliYunSmsServiceImpl());

        ReflectionTestUtils.setField(smsService, "accessKeyId", "test-access-key-id");
        ReflectionTestUtils.setField(smsService, "accessKeySecret", "test-access-key-secret");
        ReflectionTestUtils.setField(smsService, "region", "cn-hangzhou");
        ReflectionTestUtils.setField(smsService, "signName", "速通互联验证码");
        ReflectionTestUtils.setField(smsService, "templateCode", "100001");
        ReflectionTestUtils.setField(smsService, "countryCode", "86");
        ReflectionTestUtils.setField(smsService, "schemeName", "默认方案");
        ReflectionTestUtils.setField(smsService, "validTime", 300);
        ReflectionTestUtils.setField(smsService, "interval", 60);
        ReflectionTestUtils.setField(smsService, "codeLength", 4);
        ReflectionTestUtils.setField(smsService, "codeType", 1);
        ReflectionTestUtils.setField(smsService, "stringRedisTemplate", stringRedisTemplate);

        lenient().doReturn(mockClient).when(smsService).createClient();
    }

    @Test
    @DisplayName("sendSms: 阿里云返回成功时应正常发送")
    void sendSms_shouldSucceedWhenAliyunResponseOk() throws Exception {
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        body.setSuccess(true);
        body.setCode("OK");
        body.setMessage("成功");
        body.setRequestId("test-request-id");
        response.setBody(body);

        when(mockClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> smsService.sendSms("13800138000", "1234"));

        ArgumentCaptor<SendSmsVerifyCodeRequest> captor = ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(mockClient).sendSmsVerifyCode(captor.capture());

        SendSmsVerifyCodeRequest request = captor.getValue();
        assertEquals("13800138000", request.getPhoneNumber());
        assertTrue(request.getTemplateParam().contains("\"code\":\"1234\""));
        assertTrue(request.getTemplateParam().contains("\"min\":\"5\""));
    }

    @Test
    @DisplayName("sendSms: 阿里云返回失败时应抛出异常")
    void sendSms_shouldThrowWhenAliyunResponseFailed() throws Exception {
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        body.setSuccess(false);
        body.setCode("biz.FREQUENCY");
        body.setMessage("check frequency failed");
        response.setBody(body);

        when(mockClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> smsService.sendSms("13800138000", "1234"));

        assertTrue(ex.getMessage().contains("Aliyun SMS send failed"));
        assertTrue(ex.getMessage().contains("biz.FREQUENCY"));
    }

    @Test
    @DisplayName("getProviderName: 应返回Aliyun")
    void getProviderName_shouldReturnAliyun() {
        assertEquals("Aliyun", smsService.getProviderName());
    }

    @Test
    @DisplayName("sendSms: 非法手机号应抛出参数异常")
    void sendSms_shouldThrowWhenPhoneInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> smsService.sendSms("12345", "1234"));
        assertTrue(ex.getMessage().contains("Invalid phone number"));
        verifyNoInteractions(mockClient);
    }

    @Test
    @DisplayName("sendSms: 配置缺失应抛出状态异常")
    void sendSms_shouldThrowWhenConfigIncomplete() {
        ReflectionTestUtils.setField(smsService, "templateCode", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> smsService.sendSms("13800138000", "1234"));
        assertTrue(ex.getMessage().contains("config is incomplete"));
        verifyNoInteractions(mockClient);
    }

    @Test
    @DisplayName("sendSms: 返回体为空时应抛出未知错误")
    void sendSms_shouldThrowWhenResponseBodyNull() throws Exception {
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        response.setBody(null);
        when(mockClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> smsService.sendSms("13800138000", "1234"));

        assertTrue(ex.getMessage().contains("Aliyun SMS send failed"));
        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    @DisplayName("sendSms: 阿里云客户端抛异常时应透传")
    void sendSms_shouldThrowWhenClientThrows() throws Exception {
        doThrow(new RuntimeException("network error"))
                .when(mockClient)
                .sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> smsService.sendSms("13800138000", "1234"));
        assertTrue(ex.getMessage().contains("network error"));
    }

    @Test
    @DisplayName("sendSms: 带国家码手机号应被标准化")
    void sendSms_shouldNormalizePhoneWithCountryCode() throws Exception {
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        body.setSuccess(true);
        body.setCode("OK");
        body.setMessage("成功");
        body.setRequestId("test-request-id-2");
        response.setBody(body);

        when(mockClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

        smsService.sendSms("+86 138-0013-8000", "4321");

        ArgumentCaptor<SendSmsVerifyCodeRequest> captor = ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(mockClient, times(1)).sendSmsVerifyCode(captor.capture());

        SendSmsVerifyCodeRequest request = captor.getValue();
        assertEquals("13800138000", request.getPhoneNumber());
        assertEquals("86", request.getCountryCode());
    }

    @Test
    @DisplayName("createClient: 应按配置创建阿里云Client")
    void createClient_shouldCreateClientWithConfiguredFields() throws Exception {
        AliYunSmsServiceImpl realService = new AliYunSmsServiceImpl();
        ReflectionTestUtils.setField(realService, "accessKeyId", "real-test-ak");
        ReflectionTestUtils.setField(realService, "accessKeySecret", "real-test-sk");
        ReflectionTestUtils.setField(realService, "region", "cn-hangzhou");

        Client client = realService.createClient();
        assertNotNull(client);
    }
}
