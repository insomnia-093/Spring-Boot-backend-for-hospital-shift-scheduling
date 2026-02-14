package org.example.hospital.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger("CLIENT_ERROR_LOG");

    @PostMapping("/error")
    public ResponseEntity<Void> logClientError(@RequestBody Map<String, Object> error) {
        String type = String.valueOf(error.getOrDefault("type", "unknown"));
        String message = String.valueOf(error.getOrDefault("message", "未知错误"));
        String url = String.valueOf(error.getOrDefault("url", ""));

        String logMessage = String.format("[%s] %s - %s", type, message, url);

        if ("uncaught-error".equals(type)) {
            String stack = String.valueOf(error.getOrDefault("stack", ""));
            logger.error("前端未捕获错误: {}\n堆栈: {}", logMessage, stack);
        } else if ("unhandled-rejection".equals(type)) {
            logger.error("Promise 拒绝: {}", logMessage);
        } else {
            logger.warn("前端错误: {}", logMessage);
        }

        return ResponseEntity.ok().build();
    }
}
