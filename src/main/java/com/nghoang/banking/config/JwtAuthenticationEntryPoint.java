package com.nghoang.banking.config;

import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

//mặc định khi token sai/thiếu thì hệ thống trả về lỗi 401, Spring Security sẽ tự trả về response HTML hoặc plain text => ko phù hợp với REST API (luôn cần trả về JSON)
//=> JwtAuthenticationEntryPoint này được thiết kế để override lại hành vi của AuthenticationEntryPoint để khi có lỗi 401, hệ thống sẽ có thể trả về 1 ApiResponse (toàn JSON) phù hợp với tiêu chuẩn của hệ thống REST API
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint { //interface dc springsecurity định nghĩa cho dev để custom lại cách phản ứng của hệ thống khi xảy ra lỗi 401
    @Override
    //sdung httpservletresponse trực tiếp vì tầng filter chưa vào Spring MVC, bắt buộc phải tự set status code, content type và ghi dữ liệu ra I/O Stream
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        //tự tạo objectmapper
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer(); //xả toàn bộ dữ liệu còn đọng trong bộ nhớ đệm ra mạng
    }
    //Bản chất thực sự ở đây là:
    //Client (React, Postman, Mobile App) và Server đang nối với nhau qua một đường dây mạng (Socket TCP/IP).
    //Lệnh response.getWriter() mở một luồng ghi (Output Stream) hướng về phía Client.
    //Lệnh .write() và .flushBuffer() bơm dữ liệu JSON chảy qua "đường ống" đó về màn hình của Client.
    //// 1. response.getWriter() trả về một instance của class PrintWriter (là một Character Stream)
    //PrintWriter writer = response.getWriter();
    //
    //// 2. mapper.writeValueAsString(apiResponse) chuyển đối tượng Java thành chuỗi String JSON
    //String json = mapper.writeValueAsString(apiResponse);
    //
    //// 3. Ghi chuỗi JSON đó vào luồng xuất (Stream) để gửi ra đường truyền mạng cho Client
    //writer.write(json);
    //
    //// 4. Xả toàn bộ dữ liệu còn đọng trong bộ đệm (buffer) ra mạng ngay lập tức
    //response.flushBuffer();

}
