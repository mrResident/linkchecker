package ru.resprojects.linkchecker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.resprojects.linkchecker.util.ValidationUtil;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.ErrorInfo;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * Exception handler for REST-controllers
 */
@RestControllerAdvice
public class ExceptionInfoHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionInfoHandler.class);

    private final MessageSource messageSource;

    @Autowired
    public ExceptionInfoHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = {ApplicationException.class, NotFoundException.class})
    public ResponseEntity<ErrorInfo> appError(HttpServletRequest req, ApplicationException appEx) {
        return ResponseEntity.status(appEx.getHttpStatus())
            .body(logAndGetErrorInfo(req,appEx, false,
                appEx.getType(), appEx.getPlace(), appEx.getMessages()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorInfo handleError(HttpServletRequest req, Exception e) {
        return logAndGetErrorInfo(req,e, true,
            ErrorType.APP_ERROR, ErrorPlaceType.APP);
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class,
        ConstraintViolationException.class})
    public ErrorInfo validationsError(HttpServletRequest req, Exception e) {
        String[] details;
        if (e instanceof ConstraintViolationException) {
            details = ((ConstraintViolationException) e).getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toArray(String[]::new);
        } else {
            BindingResult result = e instanceof BindException ?
                ((BindException) e).getBindingResult() : ((MethodArgumentNotValidException) e).getBindingResult();
            details = result.getFieldErrors().stream()
                .map(this::getMessage)
                .toArray(String[]::new);
        }
        return logAndGetErrorInfo(req, e, false,
            ErrorType.VALIDATION_ERROR, ErrorPlaceType.APP, details);
    }

    private ErrorInfo logAndGetErrorInfo(HttpServletRequest req, Exception e,
        boolean logException, ErrorType errorType, ErrorPlaceType placeType, String... msg) {
        Throwable rootCause = ValidationUtil.logAndGetRootCause(LOG, req, e,
            logException, errorType);
        return new ErrorInfo(req.getRequestURL().toString(),errorType, placeType,
            msg.length != 0 ? msg : new String[]{ValidationUtil.getMessage(rootCause)});
    }

    private String getMessage(MessageSourceResolvable resolvable) {
        return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }

}
