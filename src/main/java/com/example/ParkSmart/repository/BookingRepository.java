package com.example.ParkSmart.repository;

import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBySlot(Slot slot);

    @Query("SELECT b FROM Booking b WHERE b.slot = :slot AND b.status IN ('BOOKED', 'CHECKED_IN') AND b.startTime < :requestedEndTime AND b.endTime > :requestedStartTime")
    List<Booking> findOverlappingBookings(@Param("slot") Slot slot,
                                         @Param("requestedStartTime") LocalDateTime requestedStartTime,
                                         @Param("requestedEndTime") LocalDateTime requestedEndTime);

    List<Booking> findByStatus(String status);

    Optional<Booking> findByIdAndStatus(Long id, String status);

    List<Booking> findBySlotAndStatus(Slot slot, String status);
}
