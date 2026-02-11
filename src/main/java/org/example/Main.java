package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        // 当 classpath 中存在用更高 Java 版本编译的 class 文件时，Spring 的 ASM 解析可能失败。
        // 开发环境中为了避免旧的 target/classes 导致启动失败，这里允许 Spring 在类格式不兼容时忽略（非推荐的长期方案）。
        // 正确做法是清理 target 并使用与项目一致的 JDK（推荐 JDK 17 或 21）。
        System.setProperty("spring.classformat.ignore", "true");

        SpringApplication.run(Main.class, args);
    }
}
