package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

/**
 * TODO Sprint add-controllers.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "request")
    private ItemRequest request;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;
        return Objects.equals(id, item.id) && name.equals(item.name) && description.equals(item.description) &&
                Objects.equals(available, item.available) && Objects.equals(owner, item.owner) &&
                Objects.equals(request, item.request);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + Objects.hashCode(available);
        result = 31 * result + Objects.hashCode(owner);
        result = 31 * result + Objects.hashCode(request);
        return result;
    }
}
