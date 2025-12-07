package com.clms.entity.bo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "swagger3")
public class SwaggerProperties {

    /**
     * 联系人的名称
     */
    private String name;


    /**
     * 联系人的邮箱
     */
    private String email;

    /**
     * API的标题
     */
    private String title;

    /**
     * API的描述
     */
    private String description;

    /**
     * API的版本号
     */
    private String version;

    /**
     * API的服务团队
     */
    private String termsOfServiceUrl;
}
