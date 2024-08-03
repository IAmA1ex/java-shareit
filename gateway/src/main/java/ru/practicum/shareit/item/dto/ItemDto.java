package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingForItem;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым.")
    private String name;

    @NotBlank(message = "Описание не может быть пустым.")
    private String description;

    @NotNull(message = "Не указан статус доступа.")
    private Boolean available;

    private BookingForItem lastBooking;

    private BookingForItem nextBooking;

    private List<CommentDto> comments;

    private Long requestId;

}
