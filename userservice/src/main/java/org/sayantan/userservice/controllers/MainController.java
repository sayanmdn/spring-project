package org.sayantan.userservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// create endpoint at "/" and response "This in Backend 2.1"
@RestController
public class MainController {
    @GetMapping("/")
    public String home() {
        return "This is Backend 2.1";
    }
}
