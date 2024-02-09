package com.example.apigatewayservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultResponseController {

    @GetMapping("/default-response")
    @ResponseStatus(HttpStatus.OK)
    public void defaultResponse() {
        // No need to add any logic, just returning a 200 OK status code
    }
}