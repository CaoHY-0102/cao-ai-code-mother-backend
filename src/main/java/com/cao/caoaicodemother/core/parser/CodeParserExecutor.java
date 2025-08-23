package com.cao.caoaicodemother.core.parser;

import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行对应的代码解析器
 */
public class CodeParserExecutor {

    public static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    public static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果（HTMLCodeResult 或 MultiFileCodeResult）
     */
    public static Object parseExecutor(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parseCode(codeContent);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default -> throw new IllegalArgumentException("Invalid code generation type: " + codeGenTypeEnum);
        };
    }
}
