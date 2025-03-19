package com.example.dice_talk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class DiceController {

    @GetMapping
    public String dice() {
        return "Hello Dice Talk!!";
    }

}
