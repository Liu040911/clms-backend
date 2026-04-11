package com.clms.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IAiChatService {

    @SystemMessage("你是高校讲座标签分类助手。你必须只从输入提供的可选标签中选择最匹配的一项，并且仅返回标签名称本身，不要输出解释、标点或其他内容。")
    String classifyLectureTag(@UserMessage String userMessage);
}
