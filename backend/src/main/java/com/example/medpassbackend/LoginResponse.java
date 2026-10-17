package com.example.medpassbackend;

public class LoginResponse {
    public boolean ok;
    public String role;
    public String message;

    public LoginResponse(boolean ok, String role, String message) {
        this.ok = ok;
        this.role = role;
        this.message = message;
    }
}