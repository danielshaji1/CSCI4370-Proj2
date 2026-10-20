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
@RequestMapping("/profile")
public class ProfileController {
    private final PostService posts;
    private final UserService users;
    public ProfileController(PostService posts, UserService users) { this.posts = posts; this.users = users; }

    @GetMapping
    public ModelAndView self() { return profile(users.getLoggedInUser().getUserId()); }
    @GetMapping("/{userId:[0-9]+}")
    public ModelAndView profile(@PathVariable("userId") String id) {
        var owner = posts.profileOwner(id);
        return Pages.posts("posts_page", owner.getFirstName() + " " + owner.getLastName(),
                posts.profile(id, users.getLoggedInUser().getUserId()), null);
    }
}
