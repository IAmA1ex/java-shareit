package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Предмет аренды не может быть пуст.")
    private Long itemId;

    @NotBlank(message = "Время начала аренды не может быть пустым.")
    private LocalDateTime start;

    @NotBlank(message = "Время окончания аренды не может быть пустым.")
    private LocalDateTime end;

}
