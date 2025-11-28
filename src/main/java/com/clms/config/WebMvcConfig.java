package com.clms.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
        public static String[] ORIGINS = {
                        "http://localhost:9000",
                        "http://localhost:8848",
                        "http://localhost:5173"
        };

        /**
         * 全局CORS配置
         */
        @Override
        public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**") // 适用于所有的API
                                .allowedOrigins(ORIGINS)
                                .allowedHeaders("*")
                                .allowedMethods("POST", "GET", "OPTIONS") // 允许的请求方法类型
                                .allowCredentials(true)
                                .maxAge(3600);
        }

        /**
         * 配置HTTP消息转换器，确保中文字符正确编码
         */
        @Override
        public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
                // 添加字符串转换器，使用UTF-8编码
                StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
                stringConverter.setWriteAcceptCharset(false); // 禁用Accept-Charset
                converters.add(0, stringConverter);
                
                // 配置JSON转换器，确保中文正确显示
                MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
                converters.add(jsonConverter);
        }

        /**
         * 设置静态资源映射
         * 
         * @param registry
         */
        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                // 添加静态资源映射规则
                registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
                // 配置 knife4j 的静态资源请求映射地址
                registry.addResourceHandler("/doc.html")
                                .addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
}
