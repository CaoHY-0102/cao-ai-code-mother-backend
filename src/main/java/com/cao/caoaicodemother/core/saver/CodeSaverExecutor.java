package com.cao.caoaicodemother.core.saver;

import com.cao.caoaicodemother.ai.model.HtmlCodeResult;
import com.cao.caoaicodemother.ai.model.MultiFileCodeResult;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeSaverExecutor {

    public static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER_TEMPLATE = new HtmlCodeFileSaverTemplate();
    public static final MultiFileCodeFileSaverTemplate MULTI_FILE_CODE_FILE_SAVER_TEMPLATE = new MultiFileCodeFileSaverTemplate();

    public static File executorSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_FILE_SAVER_TEMPLATE.saveCodeFile((HtmlCodeResult) codeResult);
            case MULTI_FILE -> MULTI_FILE_CODE_FILE_SAVER_TEMPLATE.saveCodeFile((MultiFileCodeResult) codeResult);
            default -> throw new IllegalArgumentException("Invalid code generation type: " + codeGenTypeEnum.getValue());
        };
    }
}
