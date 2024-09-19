package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO Sprint add-item-requests.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "creator")
    private User creator;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemRequest that = (ItemRequest) o;
        return Objects.equals(id, that.id) && description.equals(that.description) &&
                Objects.equals(created, that.created) && Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + description.hashCode();
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(creator);
        return result;
    }
}
