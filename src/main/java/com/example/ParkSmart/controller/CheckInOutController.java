package com.example.ParkSmart.controller;

import com.example.ParkSmart.dto.CheckOutRequest;
import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.CheckInOut;
import com.example.ParkSmart.service.CheckInOutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckInOutController {

    private final CheckInOutService checkInOutService;

    public CheckInOutController(CheckInOutService checkInOutService) {
        this.checkInOutService = checkInOutService;
    }

    @PostMapping("/check-in/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Long bookingId) {
        Booking booking = checkInOutService.checkIn(bookingId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        response.put("status", booking.getStatus());
        response.put("message", "Vehicle checked in successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-out/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long bookingId,
                                                       @RequestBody(required = false) CheckOutRequest request) {
        CheckInOut checkInOut = checkInOutService.checkout(bookingId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("slotNumber", checkInOut.getBooking().getSlot().getSlotNumber());
        response.put("checkInTime", checkInOut.getCheckInTime());
        response.put("bookedEndTime", checkInOut.getBooking().getEndTime());
        response.put("actualCheckoutTime", checkInOut.getCheckOutTime());
        response.put("overstayMinutes", checkInOut.getOverstayMinutes());
        response.put("penaltyAmount", checkInOut.getPenaltyAmount());
        response.put("message", "Vehicle checked out successfully.");
        return ResponseEntity.ok(response);
    }
}
