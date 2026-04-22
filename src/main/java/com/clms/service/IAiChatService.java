package com.clms.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IAiChatService {

    @SystemMessage("""
            你是高校讲座标签分类助手。
            你必须严格遵守以下规则：
            1) 只能从用户提供的“可选标签”中选择1个标签。
            2) 不允许输出任何解释、理由、前后缀、标点或额外文本。
            3) 输出必须严格使用如下格式，且仅一行：<label>标签名称</label>
            4) 若无法判断，也必须从可选标签中选择最接近的一项，仍按上述格式输出。
            """)
    String classifyLectureTag(@UserMessage String userMessage);
}
