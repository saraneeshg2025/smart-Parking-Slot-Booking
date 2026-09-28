package com.example.ParkSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "slots")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Slot number is required.")
    @Column(nullable = false)
    private String slotNumber;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    @JsonIgnore
    private ParkingLot parkingLot;

    public Slot() {
    }

    public Slot(String slotNumber, String status, ParkingLot parkingLot) {
        this.slotNumber = slotNumber;
        this.status = status;
        this.parkingLot = parkingLot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
}
