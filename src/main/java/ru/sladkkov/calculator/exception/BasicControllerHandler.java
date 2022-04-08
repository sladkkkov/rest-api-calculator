package ru.sladkkov.calculator.exception;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class BasicControllerHandler {

    @ExceptionHandler(ArithmeticException.class)
    public Object userNotFoundException(ArithmeticException ex) {
        return response(HttpStatus.BAD_REQUEST, ex);
    }

    private Object response(HttpStatus status, Exception ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", status.toString());
        return new ResponseEntity<>(body, headers, status);
    }
}
