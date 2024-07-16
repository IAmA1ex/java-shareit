package ru.practicum.shareit.booking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking_status")
public enum BookingStatus {

    WAITING, APPROVED, REJECTED;

    @Id
    private Long id;

}
