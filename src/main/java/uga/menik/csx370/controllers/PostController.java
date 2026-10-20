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
@RequestMapping("/post")
public class PostController {
    private final PostService posts;
    private final UserService users;
    private final CollectionService collections;
    public PostController(PostService posts, UserService users, CollectionService collections) {
        this.posts = posts; this.users = users; this.collections = collections;
    }
    private String viewer() { return users.getLoggedInUser().getUserId(); }

    @GetMapping("/{postId:[0-9]+}")
    public ModelAndView webpage(@PathVariable("postId") String id, @RequestParam(name = "error", required = false) String error) {
        var post = posts.one(id, viewer());
        var page = Pages.posts("posts_page", "Post and comments", java.util.List.of(post), error);
        page.addObject("savePostId", id);
        page.addObject("collections", collections.list(viewer()));
        return page;
    }
    @PostMapping("/{postId:[0-9]+}/comment")
    public String comment(@PathVariable("postId") String id, @RequestParam("comment") String text) {
        try { posts.comment(id, viewer(), text); return "redirect:/post/" + id; }
        catch (IllegalArgumentException e) { return Pages.error("/post/" + id, e.getMessage()); }
    }
    @PostMapping("/{postId:[0-9]+}/heart/{isAdd}")
    public String like(@PathVariable("postId") String id, @PathVariable("isAdd") boolean add) {
        posts.like(id, viewer(), add); return "redirect:/post/" + id;
    }
    @PostMapping("/{postId:[0-9]+}/bookmark/{isAdd}")
    public String bookmark(@PathVariable("postId") String id, @PathVariable("isAdd") boolean add) {
        posts.bookmark(id, viewer(), add); return "redirect:/post/" + id;
    }
    @GetMapping("/{postId:[0-9]+}/edit")
    public ModelAndView editForm(@PathVariable("postId") String id, @RequestParam(name = "error", required = false) String error) {
        var post = posts.one(id, viewer());
        if (!post.isEditable()) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Only the author can edit this post.");
        var page = new ModelAndView("edit_post");
        page.addObject("post", post); page.addObject("errorMessage", error);
        return page;
    }
    @PostMapping("/{postId:[0-9]+}/edit")
    public String edit(@PathVariable("postId") String id, @RequestParam("content") String text) {
        try { posts.edit(id, viewer(), text); return "redirect:/post/" + id; }
        catch (IllegalArgumentException e) { return Pages.error("/post/" + id + "/edit", e.getMessage()); }
    }
}
