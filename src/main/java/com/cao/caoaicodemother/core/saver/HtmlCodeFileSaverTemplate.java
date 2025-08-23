package com.cao.caoaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.ai.model.HtmlCodeResult;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * HTML单文件代码保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected void validateInput(HtmlCodeResult result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入结果不能为空");
        }
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(String baseDirPath, HtmlCodeResult result) {
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
    }
}
