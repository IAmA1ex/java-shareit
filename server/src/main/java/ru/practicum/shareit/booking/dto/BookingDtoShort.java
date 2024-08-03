package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoShort {

    @NotNull(message = "Предмет аренды не может быть пуст.")
    private Long itemId;

    @NotNull(message = "Время начала аренды не может быть пустым.")
    @FutureOrPresent(message = "Время старта неверно.")
    private LocalDateTime start;

    @NotNull(message = "Время окончания аренды не может быть пустым.")
    @FutureOrPresent(message = "Время окончания неверно.")
    private LocalDateTime end;

}
