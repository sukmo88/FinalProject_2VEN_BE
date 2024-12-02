package com.sysmatic2.finalbe.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/example")
@SecurityRequirement(name = "BearerAuth") // Swagger에서 인증 필요 표시
public class SwaggerController {

    @GetMapping
    public String swaggerEndpoint() {
        return "This is a secured endpoint";
    }
}