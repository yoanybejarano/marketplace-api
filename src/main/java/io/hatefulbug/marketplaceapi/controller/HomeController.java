package io.hatefulbug.marketplaceapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    @RequestMapping("/index")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Greetings");
    }

}
