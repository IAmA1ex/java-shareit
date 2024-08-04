package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final MethodArgumentNotValidException e) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]*)\\]");
        Matcher matcher = pattern.matcher(e.getMessage());
        String lastPart = "";
        while (matcher.find()) {
            lastPart = matcher.group(1);
        }
        ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации.", lastPart);
        logError(HttpStatus.BAD_REQUEST.value(), errorResponse);
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleForbidden(final MethodArgumentTypeMismatchException e) {
        ErrorResponse errorResponse = new ErrorResponse("Unknown state: UNSUPPORTED_STATUS",
                "Проблемы при конвертации. " + e.getValue());
        logError(HttpStatus.BAD_REQUEST.value(), errorResponse);
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(final RuntimeException e) {
        ErrorResponse privateErrorResponse = new ErrorResponse("Произошла непредвиденная ошибка.",
                e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Произошла непредвиденная ошибка.",
                "Ошибка на стороне сервера.");
        logError(HttpStatus.INTERNAL_SERVER_ERROR.value(), privateErrorResponse);
        return errorResponse;
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCustomException(final CustomError e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        logError(e.getStatus().value(), errorResponse);
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    private void logError(int code, ErrorResponse errorResponse) {
        log.error("!!! ОШИБКА({}) {}.", code, errorResponse.toString());
    }
}
