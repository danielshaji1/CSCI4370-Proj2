/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.services.UserService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    // UserService has user login and registration related functions.
    private final UserService userService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * This function serves the /register page.
     * See notes from LoginController.java.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("registration_page");

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        mv.addObject("errorMessage", error);

        return mv;
    }

    /**
     * This handles user registration form submissions.
     * See notes from LoginController.java.
     */
    @PostMapping
    public String register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("passwordRepeat") String passwordRepeat,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName) throws UnsupportedEncodingException {
        // Validate lengths before persistence, including BCrypt's 72-byte input limit.
        username = username.strip(); firstName = firstName.strip(); lastName = lastName.strip();
        if (username.length() < 2 || username.length() > 255 || firstName.length() < 2 || firstName.length() > 255
                || lastName.length() < 2 || lastName.length() > 255
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return Pages.error("/register", "Names and username must be 2–255 characters; password must be at most 72 UTF-8 bytes.");
        }
        // Passwords should have at least 3 chars.
        if (password.trim().length() < 3) {
            // If the password is too short redirect to the registration page
            // with an error message.
            String message = URLEncoder.encode("Passwords should have at least 3 nonempty letters.", "UTF-8");
            return "redirect:/register?error=" + message;
        }

        if (!password.equals(passwordRepeat)) {
            // If the password repeat does not match the password redirect to the registration page
            // with an error message.
            String message = URLEncoder.encode("Passwords do not match.", "UTF-8");
            return "redirect:/register?error=" + message;
        }

        try {
            boolean registrationSuccess = userService.registerUser(username,
                    password, firstName, lastName);
            if (registrationSuccess) {
                // If the registration worked redirect to the login page.
                return "redirect:/login";
            } else {
                // If the registration fails redirect to registration page with a message.
                String message = URLEncoder
                        .encode("Registration failed. Please try again.", "UTF-8");
                return "redirect:/register?error=" + message;
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RegistrationController.class).warn("Registration failed", e);
            // If the registration fails redirect to registration page with a message.
            String message = URLEncoder
                    .encode("Registration failed. Choose a different username or try again.", "UTF-8");
            return "redirect:/register?error=" + message;
        }
    }

}
