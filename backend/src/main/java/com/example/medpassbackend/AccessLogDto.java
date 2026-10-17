package com.example.medpassbackend;


import java.time.LocalDateTime;


public class AccessLogDto {
    public LocalDateTime date;
    public String action;
    public String participantHash;


    public AccessLogDto(LocalDateTime date, String action, String participantHash) {
        this.date = date;
        this.action = action;
        this.participantHash = participantHash;
    }
}