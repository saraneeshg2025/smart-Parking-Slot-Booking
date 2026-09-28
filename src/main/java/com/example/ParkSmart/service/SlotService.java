package com.example.ParkSmart.service;

import com.example.ParkSmart.exception.InvalidBookingException;
import com.example.ParkSmart.exception.ResourceNotFoundException;
import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.ParkingLot;
import com.example.ParkSmart.model.Slot;
import com.example.ParkSmart.repository.BookingRepository;
import com.example.ParkSmart.repository.ParkingLotRepository;
import com.example.ParkSmart.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final BookingRepository bookingRepository;

    public SlotService(SlotRepository slotRepository,
                      ParkingLotRepository parkingLotRepository,
                      BookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.bookingRepository = bookingRepository;
    }

    public Slot createSlot(Long parkingLotId, Slot slot) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingLotId));

        if (slotRepository.existsByParkingLotAndSlotNumber(parkingLot, slot.getSlotNumber())) {
            throw new InvalidBookingException("Slot number " + slot.getSlotNumber() + " already exists in this parking lot.");
        }

        slot.setParkingLot(parkingLot);
        return slotRepository.save(slot);
    }

    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }

    public Slot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
    }

    public List<Slot> getSlotsByParkingLot(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingLotId));
        return slotRepository.findByParkingLot(parkingLot);
    }

    public List<Slot> getAvailableSlotsForTimeWindow(Long parkingLotId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidBookingException("Start time and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidBookingException("End time must be after start time.");
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingLotId));

        List<Slot> allSlots = slotRepository.findByParkingLot(parkingLot);
        List<Slot> availableSlots = new ArrayList<>();

        for (Slot slot : allSlots) {
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(slot, startTime, endTime);
            if (overlappingBookings.isEmpty()) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    public Slot updateSlot(Long id, Slot slotDetails) {
        Slot existingSlot = getSlotById(id);
        existingSlot.setSlotNumber(slotDetails.getSlotNumber());
        existingSlot.setStatus(slotDetails.getStatus());
        return slotRepository.save(existingSlot);
    }

    public void deleteSlot(Long id) {
        Slot slot = getSlotById(id);
        slotRepository.delete(slot);
    }
}
