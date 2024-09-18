package ru.practicum.shareit.exception;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ErrorHandlerTest {

    private final ErrorHandler errorHandler;

    @Test
    void handleNotFound() {
        NotFoundException notFoundException = new NotFoundException("not found");

        ErrorResponse errorResponse = errorHandler.handleNotFound(notFoundException);
        assertNotNull(errorResponse);
        assertEquals("Искомый объект не найден.", errorResponse.getError());
        assertEquals(notFoundException.getMessage(), errorResponse.getDescription());
    }

    @Test
    void handleDuplicatedData() {
        DuplicatedDataException duplicatedDataException = new DuplicatedDataException("duplicated data");

        ErrorResponse errorResponse = errorHandler.handleDuplicatedData(duplicatedDataException);
        assertNotNull(errorResponse);
        assertEquals("Дублирование уникального поля.", errorResponse.getError());
        assertEquals(duplicatedDataException.getMessage(), errorResponse.getDescription());
    }

    @Test
    void handleValidationValidationException() {
        ValidationException validationException = new ValidationException("validation exception");

        ErrorResponse errorResponse = errorHandler.handleValidation(validationException);
        assertNotNull(errorResponse);
        assertEquals("Ошибка валидации.", errorResponse.getError());
        assertEquals(validationException.getMessage(), errorResponse.getDescription());
    }

    @Test
    void handleValidationBadRequestException() {
        BadRequestException badRequestException = new BadRequestException("bad request");

        ErrorResponse errorResponse = errorHandler.handleValidation(badRequestException);
        assertNotNull(errorResponse);
        assertEquals("Ошибка запроса.", errorResponse.getError());
        assertEquals(badRequestException.getMessage(), errorResponse.getDescription());
    }

    @Test
    void handleForbidden() {
        ForbiddenException forbiddenException = new ForbiddenException("forbidden");

        ErrorResponse errorResponse = errorHandler.handleForbidden(forbiddenException);
        assertNotNull(errorResponse);
        assertEquals("Запрещено.", errorResponse.getError());
        assertEquals(forbiddenException.getMessage(), errorResponse.getDescription());
    }

    @Test
    void testHandleForbiddenMethodArgumentTypeMismatchException() {
        String invalidValue = "notAnInteger";
        try {
            int value = Integer.parseInt(invalidValue);
        } catch (NumberFormatException e) {
            Method method = getClass().getMethods()[0];
            MethodParameter methodParameter = new MethodParameter(method, 0);
            MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(invalidValue,
                    Integer.class, "name", methodParameter, e);
            ErrorResponse errorResponse = errorHandler.handleForbidden(exception);
            assertNotNull(errorResponse);
            assertEquals("Unknown state: UNSUPPORTED_STATUS", errorResponse.getError());
            assertEquals("Проблемы при конвертации. notAnInteger", errorResponse.getDescription());
        }
    }

    @Test
    void handleRuntimeException() {
        RuntimeException runtimeException = new RuntimeException("runtimeException");

        ErrorResponse errorResponse = errorHandler.handleRuntimeException(runtimeException);
        assertNotNull(errorResponse);
        assertEquals("Произошла непредвиденная ошибка.", errorResponse.getError());
        assertEquals("Ошибка на стороне сервера.", errorResponse.getDescription());
    }

    @Test
    void handleCustomException() {

        ErrorResponse errorResponse = new ErrorResponse("error", "description");
        CustomError customError = new CustomError(HttpStatus.OK, errorResponse);
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleCustomException(customError);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(errorResponse.getError(), responseEntity.getBody().getError());
        assertEquals(errorResponse.getDescription(), responseEntity.getBody().getDescription());
    }
}