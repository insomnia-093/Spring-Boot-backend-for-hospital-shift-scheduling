package org.example.hospital.dto;

/**
 * Coze API 响应 DTO
 */
public class CozeResponse {
    private String response;
    private String status;
    private String error;

    public CozeResponse() {}

    public CozeResponse(String response, String status) {
        this.response = response;
        this.status = status;
    }

    public CozeResponse(String response, String status, String error) {
        this.response = response;
        this.status = status;
        this.error = error;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
