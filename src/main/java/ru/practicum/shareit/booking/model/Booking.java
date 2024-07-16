package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_user")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "renter_user")
    private User renter;

    @JoinColumn(name = "start_time")
    private LocalDateTime startTime;

    @JoinColumn(name = "end_time")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "rented_item")
    private Item item;

    @ManyToOne
    @Enumerated(EnumType.ORDINAL)
    private BookingStatus status;

}
