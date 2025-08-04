package com.bifrost.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @RequestMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity.notFound().build();
    }
}
