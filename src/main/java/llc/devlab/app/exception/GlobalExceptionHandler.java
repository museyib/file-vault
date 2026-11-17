package llc.devlab.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedFileType(UnsupportedFileTypeException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadSizeLimitException(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler({BadRequestException.class, ConstraintViolationException.class, MissingServletRequestPartException.class})
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler({UnauthorizedException.class})
    ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    ResponseEntity<ApiErrorResponse> handleForbidden(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Optimistic locking failure occurred.", exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldValidationError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldValidationError)
                .toList();

        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), exception, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(AppException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), exception, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.", exception, request.getRequestURI(), List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            Exception e,
            String path,
            List<FieldValidationError> fieldErrors
    ) {
        log.error("Exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        ));
    }

    private FieldValidationError toFieldValidationError(FieldError fieldError) {
        return new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
