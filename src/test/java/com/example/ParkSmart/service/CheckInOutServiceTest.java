package com.example.ParkSmart.service;

import com.example.ParkSmart.repository.BookingRepository;
import com.example.ParkSmart.repository.CheckInOutRepository;
import com.example.ParkSmart.repository.SlotRepository;
import com.example.ParkSmart.dto.CheckOutRequest;
import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.CheckInOut;
import com.example.ParkSmart.model.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckInOutServiceTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final CheckInOutRepository checkInOutRepository = mock(CheckInOutRepository.class);
    private final SlotRepository slotRepository = mock(SlotRepository.class);
    private CheckInOutService service;

    @BeforeEach
    void setUp() {
        service = new CheckInOutService(bookingRepository, checkInOutRepository, slotRepository);
        ReflectionTestUtils.setField(service, "penaltyRatePerHour", 50.0);
    }

    @Test
    void chargesNextHourWhenOverstayPassesHourBoundaryBySeconds() {
        LocalDateTime bookedEnd = LocalDateTime.of(2026, 9, 28, 12, 0);

        long overstayMinutes = service.calculateOverstayMinutes(
                bookedEnd,
                bookedEnd.plusHours(1).plusSeconds(1));

        assertEquals(100.0, service.calculatePenalty(overstayMinutes));
    }

    @Test
    void checkoutPersistsPenaltyCompletesBookingAndReleasesSlot() {
        Long bookingId = 12L;
        LocalDateTime bookedEnd = LocalDateTime.of(2026, 9, 28, 12, 0);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setEndTime(bookedEnd);
        booking.setStatus("CHECKED_IN");

        Slot slot = new Slot();
        slot.setStatus("OCCUPIED");
        booking.setSlot(slot);

        CheckInOut checkInOut = new CheckInOut();
        checkInOut.setBooking(booking);
        checkInOut.setCheckInTime(bookedEnd.minusMinutes(30));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(checkInOutRepository.findByBooking(booking)).thenReturn(Optional.of(checkInOut));

        CheckOutRequest request = new CheckOutRequest();
        request.setCheckOutTime(bookedEnd.plusHours(1).plusSeconds(1));
        CheckInOut result = service.checkout(bookingId, request);

        assertEquals(61L, result.getOverstayMinutes());
        assertEquals(100.0, result.getPenaltyAmount());
        assertEquals("COMPLETED", booking.getStatus());
        assertEquals("AVAILABLE", slot.getStatus());
        verify(checkInOutRepository).save(checkInOut);
        verify(bookingRepository).save(booking);
        verify(slotRepository).save(slot);
    }
}