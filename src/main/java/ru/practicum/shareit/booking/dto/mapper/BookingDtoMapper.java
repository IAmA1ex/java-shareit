package ru.practicum.shareit.booking.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
@RequiredArgsConstructor
public class BookingDtoMapper {

    private final MapperItemDto mapperItemDto;

    public BookingDto toBookingDto(final Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .status(booking.getStatus())
                .booker(booking.getRenter())
                .item(mapperItemDto.toItemDto(booking.getItem()))
                .build();
    }

    public Booking toBooking(final BookingDtoShort bookingDtoShort, final User owner,
                             final User renter, final Item item) {
        return Booking.builder()
                .owner(owner)
                .renter(renter)
                .startTime(bookingDtoShort.getStart())
                .endTime(bookingDtoShort.getEnd())
                .item(item)
                .build();
    }
}
