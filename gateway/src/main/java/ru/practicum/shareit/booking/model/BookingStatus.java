package ru.practicum.shareit.booking.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BookingStatus {

    WAITING(1L), APPROVED(2L), REJECTED(3L);

    private final Long id;

    BookingStatus(Long id) {
        this.id = id;
    }

    public static BookingStatus fromId(Long id) {
        for (BookingStatus status : BookingStatus.values()) {
            if (Objects.equals(status.getId(), id)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неверно значение для BookingStatus enum: " + id);
    }
}
