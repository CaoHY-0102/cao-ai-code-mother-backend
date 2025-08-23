package com.cao.caoaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    public static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    public final File saveCodeFile(T result) {
        //1. 验证输入
        validateInput(result);
        //2. 构建唯一目录
        String baseDirPath = buildUniqueDir();
        //3. 保存文件（具体实现由子类实现）
        saveFiles(baseDirPath, result);
        //4. 返回文件对象
        return new File(baseDirPath);
    }

    protected abstract void validateInput(T result);


    // 构建唯一目录路径： tmp/code_output/bizType_雪花ID
    protected final String buildUniqueDir() {
        String bizType = getCodeType().getValue();
        String uniqueDirName = String.format("%s_%s", bizType, IdUtil.getSnowflakeNextId());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    //写入单个文件
    protected final void writeFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();


    protected abstract void saveFiles(String dirPath, T result);


}
