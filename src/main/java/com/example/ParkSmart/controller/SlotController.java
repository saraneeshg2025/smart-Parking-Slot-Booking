package com.example.ParkSmart.controller;

import com.example.ParkSmart.model.Slot;
import com.example.ParkSmart.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/slots")
    public ResponseEntity<Slot> createSlot(@RequestParam Long parkingLotId, @Valid @RequestBody Slot slot) {
        return new ResponseEntity<>(slotService.createSlot(parkingLotId, slot), HttpStatus.CREATED);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<Slot>> getAllSlots() {
        return ResponseEntity.ok(slotService.getAllSlots());
    }

    @GetMapping("/slots/{id}")
    public ResponseEntity<Slot> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.getSlotById(id));
    }

    @GetMapping("/parking-lots/{parkingLotId}/slots")
    public ResponseEntity<List<Slot>> getSlotsByParkingLot(@PathVariable Long parkingLotId) {
        return ResponseEntity.ok(slotService.getSlotsByParkingLot(parkingLotId));
    }

    @GetMapping("/parking-lots/{parkingLotId}/available-slots")
    public ResponseEntity<List<Slot>> getAvailableSlots(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(slotService.getAvailableSlotsForTimeWindow(parkingLotId, startTime, endTime));
    }

    @PutMapping("/slots/{id}")
    public ResponseEntity<Slot> updateSlot(@PathVariable Long id, @Valid @RequestBody Slot slot) {
        return ResponseEntity.ok(slotService.updateSlot(id, slot));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<String> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.ok("Slot deleted successfully.");
    }
}
