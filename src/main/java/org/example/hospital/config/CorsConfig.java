package org.example.hospital.config;

/**
 * CORS 配置已移至 SecurityConfig
 *
 * 注意：只保留一个 CORS 配置源，避免冲突。
 * 当 allowCredentials=true 时，allowedOrigins 不能使用通配符 "*"，
 * 必须明确指定允许的源地址。
 *
 * @deprecated 使用 SecurityConfig 中的 corsConfigurationSource() 替代
 */
@Deprecated
public class CorsConfig {
    // 已弃用 - CORS 配置位于 SecurityConfig
}
