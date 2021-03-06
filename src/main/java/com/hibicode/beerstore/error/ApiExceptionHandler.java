package com.hibicode.beerstore.error;

import java.util.Locale;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hibicode.beerstore.error.ErrorResponse.ApiError;

import lombok.RequiredArgsConstructor;

/**Essa classe precisa de comunicar com o arquivo api_errors.properties e ele só consegue fazer isso através da classe ApiErrorConfig da configuração 
 * 	da classe MessageSource*/
 
//captura e centraliza as excessões aqui nessa classe e traz algumas facilidades para manipular os objetos rest
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private static final String NO_MESSSAGE_AVAILABLE = "No message available";
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource apiErrorMessageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValidException(MethodArgumentNotValidException exception
            , Locale locale) {
        Stream<ObjectError> errors = exception.getBindingResult().getAllErrors().stream();

        List<ApiError> apiErrors = errors
                .map(ObjectError::getDefaultMessage)
                .map(code -> toApiError(code, locale))
                .collect(toList());

        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, apiErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private ApiError toApiError(String code, Locale locale) {
        String message;
        try {
            message = apiErrorMessageSource.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            LOG.error("Could not find any message for {} code under {} locale", code, locale);
            message = NO_MESSSAGE_AVAILABLE;
        }

        return new ApiError(code, message);
    }

}