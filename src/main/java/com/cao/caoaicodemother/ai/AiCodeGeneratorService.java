package com.cao.caoaicodemother.ai;

import com.cao.caoaicodemother.ai.model.HtmlCodeResult;
import com.cao.caoaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 代码生成服务
 */
public interface AiCodeGeneratorService {

    /**
     * 生成 Html 代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "/prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(@UserMessage String userMessage);


    /**
     * 生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(@UserMessage String userMessage);

    /**
     * 生成 Html 代码(流式)
     *
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "/prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(@UserMessage String userMessage);


    /**
     * 生成多文件代码(流式)
     *
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(@UserMessage String userMessage);
}
