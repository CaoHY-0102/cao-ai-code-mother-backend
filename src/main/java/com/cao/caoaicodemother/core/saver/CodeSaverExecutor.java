package com.cao.caoaicodemother.core.saver;

import com.cao.caoaicodemother.ai.model.HtmlCodeResult;
import com.cao.caoaicodemother.ai.model.MultiFileCodeResult;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeSaverExecutor {

    public static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER_TEMPLATE = new HtmlCodeFileSaverTemplate();
    public static final MultiFileCodeFileSaverTemplate MULTI_FILE_CODE_FILE_SAVER_TEMPLATE = new MultiFileCodeFileSaverTemplate();

    /**
     * 根据代码生成类型执行保存
     *
     * @param codeResult      代码生成结果
     * @param codeGenTypeEnum 代码生成类型
     * @param appId 应用ID
     * @return 保存后的文件目录
     */
    public static File executorSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_FILE_SAVER_TEMPLATE.saveCodeFile((HtmlCodeResult) codeResult,appId);
            case MULTI_FILE -> MULTI_FILE_CODE_FILE_SAVER_TEMPLATE.saveCodeFile((MultiFileCodeResult) codeResult,appId);
            default -> throw new IllegalArgumentException("Invalid code generation type: " + codeGenTypeEnum.getValue());
        };
    }
}
