package ru.practicum.shareit.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.CustomError;
import ru.practicum.shareit.exception.ErrorResponse;

import java.text.SimpleDateFormat;

@Component
public class ResponseHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseHandler() {
        objectMapper.findAndRegisterModules();
    }

    public <T> T handleResponse(ResponseEntity<Object> reo, TypeReference<T> objectType) {
        handleError(reo);
        return getBodyClass(reo, objectType);
    }

    public <T> T getBodyClass(ResponseEntity<Object> reo, TypeReference<T> objectType) {
        try {
            if (reo.getBody() != null) {
                String json = objectMapper.writeValueAsString(reo.getBody());
                return objectMapper.readValue(json, objectType);
            } else return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void handleError(ResponseEntity<Object> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            int statusCode = response.getStatusCode().value();
            ErrorResponse errorResponse = getBodyClass(response, new TypeReference<ErrorResponse>() {});
            throw new CustomError(HttpStatus.valueOf(statusCode), errorResponse);
        }
    }
}
