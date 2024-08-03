package ru.practicum.shareit.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class CustomError extends RuntimeException {
    private final HttpStatus status;
    private final ErrorResponse errorResponse;
}
