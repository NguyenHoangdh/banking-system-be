package com.nghoang.banking;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Bank App",
                description = "Backend Rest APIs for Bank App",
                version = "v1.0",
                contact = @Contact(
                        name = "Nguyen Hoang",
                        email = "hoangpmhuevn@gmail.com",
                        url = "https://github.com/NguyenHoangdb"
                ),
                license = @License(
                        name = "Banking App",
                        url = "https://github.com/NguyenHoangdb"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Banking App Documentary",
                url = "https://github.com/NguyenHoangdb"
        )
) //xác định số thuộc tính
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

}
