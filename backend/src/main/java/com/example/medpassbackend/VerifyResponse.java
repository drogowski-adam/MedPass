package com.example.medpassbackend;

import java.util.List;

public class VerifyResponse {
    public boolean found;
    public String diseaseName;
    public List<String> allowedItems;

    public VerifyResponse(boolean found, String diseaseName, List<String> allowedItems) {
        this.found = found;
        this.diseaseName = diseaseName;
        this.allowedItems = allowedItems;
    }
}
