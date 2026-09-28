package com.example.ParkSmart.repository;

import com.example.ParkSmart.model.Booking;
import com.example.ParkSmart.model.CheckInOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckInOutRepository extends JpaRepository<CheckInOut, Long> {
    Optional<CheckInOut> findByBooking(Booking booking);
    boolean existsByBooking(Booking booking);
}
