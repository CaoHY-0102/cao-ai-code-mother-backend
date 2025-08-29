package com.cao.caoaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.constant.AppConstant;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    public static final String FILE_SAVE_ROOT_PATH = AppConstant.CODE_OUTPUT_ROOT_DIR;
    /**
     * 保存代码文件(使用appId)
     * @param result 代码结果对象
     * @param appId 应用ID
     * @return 保存的目录
     */
    public final File saveCodeFile(T result,Long appId) {
        //1. 验证输入
        validateInput(result);
        //2. 构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        //3. 保存文件（具体实现由子类实现）
        saveFiles(baseDirPath, result);
        //4. 返回文件对象
        return new File(baseDirPath);
    }

    protected abstract void validateInput(T result);


    /**
     * 构建基于appId的目录
     * @param appId 应用ID
     * @return 目录路径
     */
    protected final String buildUniqueDir(Long appId) {
        String bizType = getCodeType().getValue();
        String uniqueDirName = String.format("%s_%s", bizType, appId);
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
