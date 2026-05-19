package cehhghost.videohosting.video_managment.handlers;

import cehhghost.videohosting.video_managment.dtos.ErrorResponseDTO;
import cehhghost.videohosting.video_managment.exceptions.InvalidUploadStatusException;
import cehhghost.videohosting.video_managment.exceptions.VideoStorageObjectNotFoundException;
import cehhghost.videohosting.video_managment.exceptions.VideoUploadNotCompletedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(VideoStorageObjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleVideoNotFoundException(
            VideoStorageObjectNotFoundException exception,
            HttpServletRequest request
    ) {
        ErrorResponseDTO responseDTO = buildErrorResponseDTO(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
    }

    @ExceptionHandler(VideoUploadNotCompletedException.class)
    public ResponseEntity<ErrorResponseDTO> handleVideoUploadNotCompletedException(
            VideoUploadNotCompletedException exception,
            HttpServletRequest request
    ) {
        ErrorResponseDTO responseDTO = buildErrorResponseDTO(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
    }

    @ExceptionHandler(InvalidUploadStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidVideoStatusException(
            InvalidUploadStatusException exception,
            HttpServletRequest request
    ) {
        ErrorResponseDTO responseDTO = buildErrorResponseDTO(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        ErrorResponseDTO responseDTO = buildErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
    }

    private ErrorResponseDTO buildErrorResponseDTO(
            HttpStatus status,
            String message,
            String path
    ) {
        return ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Validation error";
        }

        ErrorResponseDTO responseDTO = buildErrorResponseDTO(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
    }
}
