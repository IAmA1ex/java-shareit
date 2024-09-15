package ru.practicum.shareit.booking.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ItemRepository itemRepository;

    @Test
    void findAllByRenterIdOrderByIdDesc() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);
        User user3 = getUser(3L);
        userRepository.save(user3);

        Item item1 = getItem(1L, user1);
        Item item2 = getItem(2L, user1);
        itemRepository.save(item1);
        itemRepository.save(item2);

        Booking booking1 = getBooking(1L, user2, item1, BookingStatus.REJECTED);
        Booking booking2 = getBooking(2L, user3, item1, BookingStatus.APPROVED);
        Booking booking3 = getBooking(3L, user1, item2, BookingStatus.REJECTED);
        Booking booking4 = getBooking(4L, user3, item2, BookingStatus.APPROVED);
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.findAllByRenterIdOrderByIdDesc(user3.getId());
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(user3.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
    }

    @Test
    void findAllByOwnerIdOrderByIdDesc() {
        User owner = userRepository.save(getUser(1L));
        User renter = userRepository.save(getUser(2L));

        Item item1 = itemRepository.save(getItem(1L, owner));
        Item item2 = itemRepository.save(getItem(2L, owner));

        Booking booking1 = bookingRepository.save(getBooking(1L, renter, item1, BookingStatus.APPROVED));
        Booking booking2 = bookingRepository.save(getBooking(2L, renter, item2, BookingStatus.APPROVED));

        List<Booking> bookings = bookingRepository.findAllByOwnerIdOrderByIdDesc(owner.getId());
        assertEquals(2, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
        assertEquals(booking1.getId(), bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));

    }

    @Test
    void getBookingsCurrentForRenter() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusHours(1), now.plusHours(1));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.minusHours(2), now.plusHours(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> bookings = bookingRepository.getBookingsCurrentForRenter(renter.getId());

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(renter.getId())));
        assertTrue(bookings.get(0).getId() < bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isBefore(now) && booking.getEndTime().isAfter(now)));
    }

    @Test
    void getBookingsPastForRenter() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(2));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsPastForRenter(renter.getId(), BookingStatus.APPROVED);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(renter.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isBefore(now) &&
                booking.getEndTime().isBefore(now) && booking.getStartTime().isBefore(booking.getEndTime())));
    }

    @Test
    void getBookingsFutureForRenter() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsFutureForRenter(renter.getId(), BookingStatus.WAITING,
                BookingStatus.APPROVED);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(renter.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isAfter(now) &&
                booking.getEndTime().isAfter(now) && booking.getStartTime().isBefore(booking.getEndTime())));
    }

    @Test
    void getBookingsWaitingForRenter() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.WAITING, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.WAITING, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsWaitingForRenter(renter.getId(), BookingStatus.WAITING);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(renter.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(b -> b.getStatus().equals(BookingStatus.WAITING)));
    }

    @Test
    void getBookingsRejectedForRenter() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.REJECTED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.REJECTED, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsRejectedForRenter(renter.getId(), BookingStatus.REJECTED);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getRenter().getId().equals(renter.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(b -> b.getStatus().equals(BookingStatus.REJECTED)));
    }

    @Test
    void getBookingsCurrentForOwner() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusHours(1), now.plusHours(1));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.minusHours(2), now.plusHours(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> bookings = bookingRepository.getBookingsCurrentForOwner(owner.getId());

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));
        assertTrue(bookings.get(0).getId() < bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isBefore(now) && booking.getEndTime().isAfter(now)));
    }

    @Test
    void getBookingsPastForOwner() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(2));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsPastForOwner(owner.getId(), BookingStatus.APPROVED);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isBefore(now) &&
                booking.getEndTime().isBefore(now) && booking.getStartTime().isBefore(booking.getEndTime())));
    }

    @Test
    void getBookingsFutureForOwner() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.APPROVED, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsFutureForOwner(owner.getId(), BookingStatus.WAITING,
                BookingStatus.APPROVED);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(booking -> booking.getStartTime().isAfter(now) &&
                booking.getEndTime().isAfter(now) && booking.getStartTime().isBefore(booking.getEndTime())));
    }

    @Test
    void getBookingsWaitingForOwner() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.APPROVED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.WAITING, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.WAITING, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsWaitingForOwner(owner.getId(), BookingStatus.WAITING);

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(b -> b.getStatus().equals(BookingStatus.WAITING)));
    }

    @Test
    void getBookingsRejectedForOwner() {
        User renter = getUser(1L);
        User owner = getUser(2L);
        userRepository.save(renter);
        userRepository.save(owner);

        Item item1 = getItem(1L, owner);
        Item item2 = getItem(2L, owner);
        Item item3 = getItem(3L, owner);
        Item item4 = getItem(4L, owner);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = getBooking(1L, renter, item1, BookingStatus.REJECTED, now.minusDays(1), now.minusHours(10));
        Booking booking2 = getBooking(2L, renter, item2, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking booking3 = getBooking(3L, renter, item3, BookingStatus.REJECTED, now.plusHours(2), now.plusHours(3));
        Booking booking4 = getBooking(4L, renter, item4, BookingStatus.APPROVED, now.minusHours(3), now.plusDays(1));
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        List<Booking> bookings = bookingRepository.getBookingsRejectedForOwner(owner.getId(), BookingStatus.REJECTED);
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(booking -> booking.getItem().getOwner().getId().equals(owner.getId())));
        assertTrue(bookings.get(0).getId() > bookings.get(1).getId());
        assertTrue(bookings.stream().allMatch(b -> b.getStatus().equals(BookingStatus.REJECTED)));
    }

    @Test
    void availableAtTime() {
        User owner = getUser(1L);
        User renter = getUser(2L);
        userRepository.save(owner);
        userRepository.save(renter);

        Item item = getItem(null, owner);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking booking = getBooking(null, renter, item, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        bookingRepository.save(booking);

        boolean isAvailable1 = bookingRepository.availableAtTime(item, BookingStatus.APPROVED, now.plusDays(3), now.plusDays(4));
        assertTrue(isAvailable1);
        boolean isAvailable2 = bookingRepository.availableAtTime(item, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        assertFalse(isAvailable2);
    }

    @Test
    void getLastBooking() {
        User owner = getUser(1L);
        User renter = getUser(2L);
        userRepository.save(owner);
        userRepository.save(renter);

        Item item = getItem(null, owner);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking1 = getBooking(null, renter, item, BookingStatus.APPROVED, now.minusDays(5), now.minusDays(4));
        Booking pastBooking2 = getBooking(null, renter, item, BookingStatus.APPROVED, now.minusDays(3), now.minusDays(2));
        Booking futureBooking = getBooking(null, renter, item, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        bookingRepository.save(pastBooking1);
        bookingRepository.save(pastBooking2);
        bookingRepository.save(futureBooking);

        Booking lastBooking = bookingRepository.getLastBooking(owner.getId(), item.getId());
        assertNotNull(lastBooking);
        assertEquals(pastBooking2.getId(), lastBooking.getId());
        assertTrue(lastBooking.getStartTime().isBefore(now));
        assertTrue(lastBooking.getStartTime().isBefore(futureBooking.getStartTime()));
    }

    @Test
    void getNextBooking() {
        User owner = getUser(1L);
        User renter = getUser(2L);
        userRepository.save(owner);
        userRepository.save(renter);

        Item item = getItem(1L, owner);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = getBooking(1L, renter, item, BookingStatus.APPROVED, now.minusDays(5), now.minusDays(4));
        Booking nextBooking1 = getBooking(2L, renter, item, BookingStatus.APPROVED, now.plusDays(1), now.plusDays(2));
        Booking nextBooking2 = getBooking(3L, renter, item, BookingStatus.APPROVED, now.plusDays(3), now.plusDays(4));
        Booking futureBooking = getBooking(4L, renter, item, BookingStatus.REJECTED, now.plusDays(5), now.plusDays(6));
        bookingRepository.save(pastBooking);
        bookingRepository.save(nextBooking1);
        bookingRepository.save(nextBooking2);
        bookingRepository.save(futureBooking);

        Booking nextBooking = bookingRepository.getNextBooking(owner.getId(), item.getId(), BookingStatus.APPROVED);
        assertNotNull(nextBooking);
        assertEquals(nextBooking1.getId(), nextBooking.getId());
        assertTrue(nextBooking.getStartTime().isAfter(now));
    }

    @Test
    void isUserContainsCompletedBookingForItem() {
        User owner = getUser(1L);
        User renter = getUser(2L);
        userRepository.save(owner);
        userRepository.save(renter);

        Item item = getItem(null, owner);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking completedBooking = getBooking(null, renter, item, BookingStatus.APPROVED, now.minusDays(5), now.minusDays(3));
        Booking ongoingBooking = getBooking(null, renter, item, BookingStatus.APPROVED, now.minusDays(2), now.plusDays(2));
        bookingRepository.save(completedBooking);
        bookingRepository.save(ongoingBooking);

        List<Booking> completedBookings = bookingRepository.isUserContainsCompletedBookingForItem(
                renter.getId(), item.getId(), now);
        assertNotNull(completedBookings);
        assertEquals(1, completedBookings.size());
        assertEquals(completedBooking.getId(), completedBookings.get(0).getId());

    }

    private Booking getBooking(Long bookingId, User renter, Item item, BookingStatus bookingStatus) {
        return Booking.builder()
                .id(bookingId)
                .renter(renter)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .item(item)
                .status(bookingStatus)
                .build();
    }

    private Booking getBooking(Long bookingId, User renter, Item item, BookingStatus bookingStatus,
                               LocalDateTime start, LocalDateTime end) {
        return Booking.builder()
                .id(bookingId)
                .renter(renter)
                .startTime(start)
                .endTime(end)
                .item(item)
                .status(bookingStatus)
                .build();
    }

    private Item getItem(Long itemId, User owner) {
        return Item.builder()
                .id(itemId)
                .name("name" + itemId)
                .description("description" + itemId)
                .available(Boolean.TRUE)
                .owner(owner)
                .request(null)
                .build();
    }

    private User getUser(Long userId) {
        return User.builder()
                .id(userId)
                .name("name" + userId)
                .email("email" + userId + "@email.com")
                .build();
    }
}