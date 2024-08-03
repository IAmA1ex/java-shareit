package ru.practicum.shareit.booking.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

@Component
@RequiredArgsConstructor
public class BookingDtoMapper {

    public BookItemRequestDto toBookItemRequestDto(final BookingDtoShort bookingDtoShort) {
        return BookItemRequestDto.builder()
                .itemId(bookingDtoShort.getItemId())
                .start(bookingDtoShort.getStart())
                .end(bookingDtoShort.getEnd())
                .build();
    }
}
