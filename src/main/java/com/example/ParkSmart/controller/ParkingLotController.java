package com.example.ParkSmart.controller;

import com.example.ParkSmart.model.ParkingLot;
import com.example.ParkSmart.service.ParkingLotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @PostMapping
    public ResponseEntity<ParkingLot> createParkingLot(@Valid @RequestBody ParkingLot parkingLot) {
        return new ResponseEntity<>(parkingLotService.createParkingLot(parkingLot), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {
        return ResponseEntity.ok(parkingLotService.getAllParkingLots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.getParkingLotById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingLot> updateParkingLot(@PathVariable Long id, @Valid @RequestBody ParkingLot parkingLot) {
        return ResponseEntity.ok(parkingLotService.updateParkingLot(id, parkingLot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteParkingLot(@PathVariable Long id) {
        parkingLotService.deleteParkingLot(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Parking lot deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{parkingLotId}/occupancy")
    public ResponseEntity<Map<String, Object>> getParkingLotOccupancy(@PathVariable Long parkingLotId) {
        return ResponseEntity.ok(parkingLotService.getOccupancyDetails(parkingLotId));
    }
}
