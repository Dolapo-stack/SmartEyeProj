package com.bgham.smarteye;

/** Api class for structuring response from server */
public class ApiResponse {
    private String id;
    private String status;
    private String token;
    private String name;
    private String panic_status;

    public ApiResponse(String status, String token, String id, String name, String panic_status) {
        this.status = status;
        this.token = token;
        this.id=id;
        this.name=name;
        this.panic_status=panic_status;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPanicStatus() {
        return panic_status;
    }
    public void setPanicStatus(String panic_status) {
        this.panic_status = status;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return name;
    }
    public void setUserName(String name) {
        this.name = name;
    }


}
