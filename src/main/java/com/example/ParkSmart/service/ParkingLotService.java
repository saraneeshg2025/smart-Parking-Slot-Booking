package com.example.ParkSmart.service;

import com.example.ParkSmart.exception.ResourceNotFoundException;
import com.example.ParkSmart.model.ParkingLot;
import com.example.ParkSmart.model.Slot;
import com.example.ParkSmart.repository.ParkingLotRepository;
import com.example.ParkSmart.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final SlotRepository slotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository, SlotRepository slotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.slotRepository = slotRepository;
    }

    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        return parkingLotRepository.save(parkingLot);
    }

    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }

    public ParkingLot getParkingLotById(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
    }

    public ParkingLot updateParkingLot(Long id, ParkingLot parkingLotDetails) {
        ParkingLot existing = getParkingLotById(id);
        existing.setName(parkingLotDetails.getName());
        existing.setLocation(parkingLotDetails.getLocation());
        existing.setTotalSlots(parkingLotDetails.getTotalSlots());
        return parkingLotRepository.save(existing);
    }

    public void deleteParkingLot(Long id) {
        ParkingLot parkingLot = getParkingLotById(id);
        parkingLotRepository.delete(parkingLot);
    }

    public Map<String, Object> getOccupancyDetails(Long parkingLotId) {
        ParkingLot parkingLot = getParkingLotById(parkingLotId);
        List<Slot> slots = slotRepository.findByParkingLot(parkingLot);

        int totalSlots = parkingLot.getTotalSlots();
        long occupiedSlots = slots.stream()
                .filter(slot -> "OCCUPIED".equalsIgnoreCase(slot.getStatus()))
                .count();
        int availableSlots = totalSlots - (int) occupiedSlots;
        double occupancyPercentage = totalSlots == 0 ? 0.0 : (occupiedSlots * 100.0) / totalSlots;

        Map<String, Object> response = new HashMap<>();
        response.put("parkingLotId", parkingLotId);
        response.put("parkingLotName", parkingLot.getName());
        response.put("totalSlots", totalSlots);
        response.put("occupiedSlots", occupiedSlots);
        response.put("availableSlots", availableSlots);
        response.put("occupancyPercentage", occupancyPercentage);
        return response;
    }
}
