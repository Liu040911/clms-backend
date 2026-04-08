package com.clms.service;

import cn.hutool.json.JSONObject;

public interface IAppConfigService {

    /**
     * 获取后端应用配置
     * @return 配置JSON
     */
    JSONObject getManagerConfig(String userId);

    /**
     * 获取APP端应用配置
     * 
     * <p>
     * 获取指定应用键和应用渠道下的配置项。如果configKey为null，则返回整个配置对象。
     * </p>
     *
     * @param configKey 配置键，如果为null则返回整个配置对象
     * @return 返回指定的配置项或整个配置对象，如果配置不存在则返回空的JSONObject
    */
    JSONObject getConfig(String configKey);


}
