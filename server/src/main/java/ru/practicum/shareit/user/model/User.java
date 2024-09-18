package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * TODO Sprint add-controllers.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;
}
