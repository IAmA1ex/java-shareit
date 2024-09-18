package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingForItem;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingForItem lastBooking;

    private BookingForItem nextBooking;

    private List<CommentDto> comments;

    private Long requestId;

}
