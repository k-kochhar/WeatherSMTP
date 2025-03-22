package com.kshitijkochhar.weathersmtp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/send-email")
    public String sendEmail(@RequestParam String message, @RequestParam String recipient) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipient);
        email.setSubject("Weather Update");
        email.setText(message);
        mailSender.send(email);
        return "Message sent!";
    }
}
