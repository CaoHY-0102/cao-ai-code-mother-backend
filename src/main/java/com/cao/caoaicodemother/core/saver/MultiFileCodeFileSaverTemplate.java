package com.cao.caoaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.ai.model.MultiFileCodeResult;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    public CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(String baseDirPath, MultiFileCodeResult result) {
        // 保存 HTML 文件
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件
        writeFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件
        writeFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入结果不能为空");
        }
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
