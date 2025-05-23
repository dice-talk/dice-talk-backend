package com.example.dice_talk.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminDashBoardController {

    @GetMapping("/dashboard")
    public ResponseEntity getMainDashboard() {

        //
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
