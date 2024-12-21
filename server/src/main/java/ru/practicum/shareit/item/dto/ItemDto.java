package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingForItem;

import java.util.List;
import java.util.Objects;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingForItem lastBooking;

    private BookingForItem nextBooking;

    private List<CommentDto> comments;

    private Long requestId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemDto itemDto = (ItemDto) o;
        return Objects.equals(id, itemDto.id) && name.equals(itemDto.name) &&
                description.equals(itemDto.description) && Objects.equals(available, itemDto.available) &&
                Objects.equals(lastBooking, itemDto.lastBooking) && Objects.equals(nextBooking, itemDto.nextBooking) &&
                Objects.equals(comments, itemDto.comments) && Objects.equals(requestId, itemDto.requestId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + Objects.hashCode(available);
        result = 31 * result + Objects.hashCode(lastBooking);
        result = 31 * result + Objects.hashCode(nextBooking);
        result = 31 * result + Objects.hashCode(comments);
        result = 31 * result + Objects.hashCode(requestId);
        return result;
    }
}
