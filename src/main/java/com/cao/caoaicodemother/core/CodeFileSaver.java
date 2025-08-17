package com.cao.caoaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.cao.caoaicodemother.ai.model.HtmlCodeResult;
import com.cao.caoaicodemother.ai.model.MultiFileCodeResult;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {

    // 文件保存根目录
    public static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    // 保存HTML代码文件
    public static File saveHtmlCodeFile(HtmlCodeResult result) {
        // 1. 文件类型
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }

    // 保存多文件代码
    public static File saveMultiFileCodeFile(MultiFileCodeResult result) {
        // 1. 文件类型
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        writeFile(baseDirPath, "style.css", result.getCssCode());
        writeFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    // 构建唯一目录路径： tmp/code_output/bizType_雪花ID
    public static String buildUniqueDir(String bizType) {
        String uniqueDirName = String.format("%s_%s", bizType, IdUtil.getSnowflakeNextId());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    //写入单个文件
    public static void writeFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
