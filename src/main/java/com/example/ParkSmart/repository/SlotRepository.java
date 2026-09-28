package com.example.ParkSmart.repository;

import com.example.ParkSmart.model.ParkingLot;
import com.example.ParkSmart.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByParkingLot(ParkingLot parkingLot);
    boolean existsByParkingLotAndSlotNumber(ParkingLot parkingLot, String slotNumber);
}
