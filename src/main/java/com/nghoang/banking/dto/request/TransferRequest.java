package com.nghoang.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {
    @NotBlank(message = "Destination account number has been required")
    String destinationAccountNumber;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "INVALID_AMOUNT")
    //Khi ứng dụng chạy, thuộc tính message của Annotation sẽ đóng vai trò như một Chiếc Cầu Nối:
    //Bước 1 (Lấy tên Enum): Hibernate Validator trả về giá trị message chính là chuỗi "USERNAME_NOT_NULL".
    //Bước 2 (Áp vào Enum): Bạn lấy chuỗi đó ép ngược lại thành đối tượng Enum bằng hàm ErrorCode.valueOf("USERNAME_NOT_NULL").
    //Bước 3 (Lấy Message xịn): Từ đối tượng ErrorCode lấy ra ở Bước 2, bạn mới gọi errorCode.getMessage() để rút câu thông báo thật (ví dụ: "Tên người dùng không được để trống").
    //Bước 4 (Map Attributes nếu có): Nếu câu thông báo có chứa {min}, {max}, {value}, hàm mapAttribute mới thực hiện thay thế con số thực tế vào.
    BigDecimal amount;
}
