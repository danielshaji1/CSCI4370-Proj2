/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uga.menik.csx370.services.*;

@Controller
public class HomeController {
    private final PostService posts;
    private final UserService users;
    public HomeController(PostService posts, UserService users) { this.posts = posts; this.users = users; }

    @GetMapping("/")
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        return Pages.posts("home_page", "Home", posts.home(users.getLoggedInUser().getUserId()), error);
    }
    @PostMapping("/createpost")
    public String create(@RequestParam("posttext") String text) {
        try { posts.create(text, users.getLoggedInUser().getUserId()); return "redirect:/"; }
        catch (IllegalArgumentException e) { return Pages.error("/", e.getMessage()); }
    }
}
