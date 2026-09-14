package com.nghoang.banking.exception;

import com.nghoang.banking.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalHandlerException {
    private static final String MIN_ATTRIBUTE = "value";
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    //handle deadlock khi xảy ra:   Thread 1: A → B (lock A, chờ lock B)
    //                              Thread 2: B → A (lock B, chờ lock A) => vô tận, treo app
    @ExceptionHandler(CannotAcquireLockException.class)
    ResponseEntity<ApiResponse<?>> handlingDeadlock(CannotAcquireLockException exception) {
        ErrorCode errorCode = ErrorCode.TRANSFER_CONFLICT;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<?>> handlingException(RuntimeException exception) {
        log.error("Unhandled exception", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldError();
        //biến = (điều_kiện) ? giá_trị_nếu_đúng : giá_trị_nếu_sai;
        String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : null; //viết cách này để tránh rủi ro NEP ở cấp độ class (nếu sau này có @PasswordMatching(message = "PASSWORD_NOT_MATCH") gắn ở cấp độ class DTO)
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null; //chứa các lỗi validation
        String message = null;
        if (enumKey != null) {
            try {
                errorCode = ErrorCode.valueOf(enumKey);
                var constrainViolation = fieldError.unwrap(ConstraintViolation.class);
                attributes = constrainViolation.getConstraintDescriptor().getAttributes();
                log.info("Validation atributes: {}", attributes);
            } catch (IllegalArgumentException exception) {
                log.warn("Validation error key [{}] not found in ErrorCode enum", enumKey);
                message = enumKey;
            }
        }
//        //nếu message có giá trị nghĩa là giá trị client gửi lên xảy ra validate ở những annotation message ko có metadata, phải check trước
        if (message == null) { //trường  hợp điều kiện đúng thì xảy ra validate ở các annotation có metadata
            message = Objects.nonNull(attributes)
                    ? mapAttribute(errorCode.getMessage(), attributes)
                    : errorCode.getMessage(); //trường hợp code sai chính tả ở attribute message phần annotation bên các request
        }

        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        if (message == null || attributes == null) {
            return message;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey(); //Ví dụ: "min", "max", "value"
            String value = String.valueOf(entry.getValue()); //ví dụ: "6", "20", "18"
            //thay thế key trong message bằng value thực tế
            message = message.replace("{" + key + "}", value);
        }
        return message;
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        ErrorCode errorCode = ErrorCode.AUTHORIZATION_DENIED;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<?>> handlingDataIntegrityViolationException(DataIntegrityViolationException exception) {
        ErrorCode errorCode = ErrorCode.DATA_EXISTED_IN_ROLE;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }





}
