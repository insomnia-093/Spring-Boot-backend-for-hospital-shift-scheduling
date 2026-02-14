package org.example.hospital.dto;

/**
 * Coze API 请求 DTO
 */
public class CozeRequest {
    private String content;
    private Long userId;

    public CozeRequest() {}

    public CozeRequest(String content, Long userId) {
        this.content = content;
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
