package com.example.ParkSmart.service;

import com.example.ParkSmart.dto.BookingRequest;
import com.example.ParkSmart.exception.InvalidBookingException;
import com.example.ParkSmart.exception.ResourceNotFoundException;
import com.example.ParkSmart.exception.SlotAlreadyBookedException;
import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.Slot;
import com.example.ParkSmart.repository.BookingRepository;
import com.example.ParkSmart.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    public BookingService(BookingRepository bookingRepository, SlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    public Booking createBooking(BookingRequest request) {
        validateBookingRequest(request);

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateOverlappingBookings(slot, request.getStartTime(), request.getEndTime());

        Booking booking = new Booking();
        booking.setCustomerName(request.getCustomerName().trim());
        booking.setVehicleNumber(request.getVehicleNumber().trim());
        booking.setSlot(slot);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus("BOOKED");

        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateBooking(Long id, BookingRequest request) {
        Booking existingBooking = getBookingById(id);
        if (!"BOOKED".equals(existingBooking.getStatus())) {
            throw new InvalidBookingException("Only BOOKED bookings can be updated.");
        }

        validateBookingRequest(request);
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(slot, request.getStartTime(), request.getEndTime());
        overlapping.removeIf(booking -> booking.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new SlotAlreadyBookedException("Slot " + slot.getSlotNumber() + " is already booked for the selected time window.");
        }

        existingBooking.setCustomerName(request.getCustomerName().trim());
        existingBooking.setVehicleNumber(request.getVehicleNumber().trim());
        existingBooking.setSlot(slot);
        existingBooking.setStartTime(request.getStartTime());
        existingBooking.setEndTime(request.getEndTime());
        return bookingRepository.save(existingBooking);
    }

    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        if ("CHECKED_IN".equals(booking.getStatus())) {
            throw new InvalidBookingException("Checked-in booking cannot be cancelled.");
        }
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new InvalidBookingException("Booking request is required.");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new InvalidBookingException("Customer name is required.");
        }
        if (request.getVehicleNumber() == null || request.getVehicleNumber().isBlank()) {
            throw new InvalidBookingException("Vehicle number is required.");
        }
        if (request.getSlotId() == null || request.getSlotId() <= 0) {
            throw new InvalidBookingException("Valid slot ID is required.");
        }
        if (request.getStartTime() == null) {
            throw new InvalidBookingException("Start time is required.");
        }
        if (request.getEndTime() == null) {
            throw new InvalidBookingException("End time is required.");
        }
        validateTimeRange(request.getStartTime(), request.getEndTime());
    }

    public void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidBookingException("Start time and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidBookingException("End time must be after start time.");
        }
    }

    public void validateOverlappingBookings(Slot slot, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(slot, requestedStartTime, requestedEndTime);
        if (!overlappingBookings.isEmpty()) {
            throw new SlotAlreadyBookedException("Slot " + slot.getSlotNumber() + " is already booked for the selected time window.");
        }
    }
}
