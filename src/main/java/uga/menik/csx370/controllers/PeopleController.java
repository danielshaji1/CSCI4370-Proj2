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
@RequestMapping("/people")
public class PeopleController {
    private final PeopleService people;
    private final UserService users;
    public PeopleController(PeopleService people, UserService users) { this.people = people; this.users = users; }
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        var list = people.getFollowableUsers(users.getLoggedInUser().getUserId());
        ModelAndView page = new ModelAndView("people_page");
        page.addObject("users", list);
        page.addObject("isNoContent", list.isEmpty());
        page.addObject("errorMessage", error);
        return page;
    }
    @PostMapping("/{userId:[0-9]+}/follow/{isFollow}")
    public String follow(@PathVariable("userId") String id, @PathVariable("isFollow") boolean follow) {
        try { people.follow(users.getLoggedInUser().getUserId(), id, follow); return "redirect:/people"; }
        catch (IllegalArgumentException e) { return Pages.error("/people", e.getMessage()); }
    }
}
