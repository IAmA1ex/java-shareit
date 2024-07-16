package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByRenterId(Long userId);

    List<Booking> findAllByOwnerId(Long userId);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2 and
            current_timestamp between b.startTime and b.endTime
    """)
    List<Booking> getBookingsCurrentForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2 and
            current_timestamp > b.endTime
    """)
    List<Booking> getBookingsPastForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2 and
            current_timestamp < b.startTime
    """)
    List<Booking> getBookingsFutureForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2
    """)
    List<Booking> getBookingsWaitingForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2
    """)
    List<Booking> getBookingsRejectedForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2 and
            current_timestamp between b.startTime and b.endTime
    """)
    List<Booking> getBookingsCurrentForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2 and
            current_timestamp > b.endTime
    """)
    List<Booking> getBookingsPastForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2 and
            current_timestamp < b.startTime
    """)
    List<Booking> getBookingsFutureForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2
    """)
    List<Booking> getBookingsWaitingForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2
    """)
    List<Booking> getBookingsRejectedForOwner(Long userId, BookingStatus status);

}
