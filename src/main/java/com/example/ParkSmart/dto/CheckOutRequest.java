package com.example.ParkSmart.dto;

import java.time.LocalDateTime;

public class CheckOutRequest {

    private LocalDateTime checkOutTime;

    public CheckOutRequest() {
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
}
