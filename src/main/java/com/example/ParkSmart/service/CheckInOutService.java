package com.example.ParkSmart.service;

import com.example.ParkSmart.dto.CheckOutRequest;
import com.example.ParkSmart.exception.InvalidBookingException;
import com.example.ParkSmart.exception.ResourceNotFoundException;
import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.CheckInOut;
import com.example.ParkSmart.model.Slot;
import com.example.ParkSmart.repository.BookingRepository;
import com.example.ParkSmart.repository.CheckInOutRepository;
import com.example.ParkSmart.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CheckInOutService {

    private final BookingRepository bookingRepository;
    private final CheckInOutRepository checkInOutRepository;
    private final SlotRepository slotRepository;

    @Value("${parksmart.penalty-rate-per-hour:50}")
    private double penaltyRatePerHour;

    public CheckInOutService(BookingRepository bookingRepository,
                            CheckInOutRepository checkInOutRepository,
                            SlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.checkInOutRepository = checkInOutRepository;
        this.slotRepository = slotRepository;
    }

    public Booking checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!"BOOKED".equals(booking.getStatus())) {
            throw new InvalidBookingException("Booking is not available for check-in.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(booking.getStartTime()) || now.isAfter(booking.getEndTime())) {
            throw new InvalidBookingException("Current time is outside the booking time window.");
        }

        if (checkInOutRepository.existsByBooking(booking)) {
            throw new InvalidBookingException("Check-in has already happened for this booking.");
        }

        CheckInOut checkInOut = new CheckInOut();
        checkInOut.setBooking(booking);
        checkInOut.setCheckInTime(now);
        checkInOutRepository.save(checkInOut);

        booking.setStatus("CHECKED_IN");
        Slot slot = booking.getSlot();
        slot.setStatus("OCCUPIED");
        slotRepository.save(slot);
        return bookingRepository.save(booking);
    }

    public CheckInOut checkout(Long bookingId, CheckOutRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new InvalidBookingException("Booking must be checked in before checkout.");
        }

        CheckInOut checkInOut = checkInOutRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record not found for booking id: " + bookingId));

        if (checkInOut.getCheckOutTime() != null) {
            throw new InvalidBookingException("Checkout already happened for this booking.");
        }

        LocalDateTime actualCheckoutTime = request != null && request.getCheckOutTime() != null
                ? request.getCheckOutTime()
                : LocalDateTime.now();

        long overstayMinutes = calculateOverstayMinutes(booking.getEndTime(), actualCheckoutTime);
        double penaltyAmount = calculatePenalty(overstayMinutes);

        checkInOut.setCheckOutTime(actualCheckoutTime);
        checkInOut.setOverstayMinutes(overstayMinutes);
        checkInOut.setPenaltyAmount(penaltyAmount);
        checkInOutRepository.save(checkInOut);

        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        Slot slot = booking.getSlot();
        slot.setStatus("AVAILABLE");
        slotRepository.save(slot);

        return checkInOut;
    }

    public long calculateOverstayMinutes(LocalDateTime bookedEndTime, LocalDateTime actualCheckoutTime) {
        if (actualCheckoutTime == null || bookedEndTime == null) {
            return 0L;
        }
        if (!actualCheckoutTime.isAfter(bookedEndTime)) {
            return 0L;
        }

        Duration duration = Duration.between(bookedEndTime, actualCheckoutTime);
        return duration.toMinutes();
    }

    public double calculatePenalty(long overstayMinutes) {
        if (overstayMinutes <= 0) {
            return 0.0;
        }

        long extraHours = (long) Math.ceil(overstayMinutes / 60.0);
        return extraHours * penaltyRatePerHour;
    }

    public Optional<CheckInOut> getCheckInOutByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return checkInOutRepository.findByBooking(booking);
    }
}
