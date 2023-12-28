package com.qdp.fish.template.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUtils {

    /**
     * 读取文件的所有行
     *
     * @param filePath 文件路径
     * @return 包含文件所有行的列表
     * @throws IOException 读取文件时发生的异常
     */
    public static List<String> readAllLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * 将文本内容写入文件
     *
     * @param filePath 文件路径
     * @param content  要写入的文本内容
     * @throws IOException 写入文件时发生的异常
     */
    public static void writeToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    // 可以添加其他文件操作方法
}
