package com.example.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Controller {

    private final RestTemplate restTemplate;

    public Controller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


}
