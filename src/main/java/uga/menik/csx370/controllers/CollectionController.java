package uga.menik.csx370.controllers;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uga.menik.csx370.services.CollectionService;
import uga.menik.csx370.services.UserService;

@Controller
@RequestMapping("/collections")
public class CollectionController {
    private final CollectionService collections;
    private final UserService users;
    public CollectionController(CollectionService collections, UserService users) { this.collections = collections; this.users = users; }
    private String viewer() { return users.getLoggedInUser().getUserId(); }

    @GetMapping
    public ModelAndView list(@RequestParam(name = "error", required = false) String error) {
        var page = new ModelAndView("collections_page");
        page.addObject("collections", collections.list(viewer()));
        page.addObject("errorMessage", error);
        return page;
    }
    @PostMapping
    public String create(@RequestParam("name") String name, @RequestParam(name = "description", defaultValue = "") String description) {
        try { return "redirect:/collections/" + collections.create(viewer(), name, description); }
        catch (IllegalArgumentException e) { return Pages.error("/collections", e.getMessage()); }
        catch (DuplicateKeyException e) { return Pages.error("/collections", "You already have a collection with that name."); }
    }
    @GetMapping("/{collectionId}")
    public ModelAndView show(@PathVariable("collectionId") long id, @RequestParam(name = "error", required = false) String error) {
        var collection = collections.get(id, viewer());
        var page = Pages.posts("collection_page", collection.name(), collections.posts(id, viewer()), error);
        page.addObject("collection", collection);
        page.addObject("collectionId", id);
        return page;
    }
    @PostMapping("/{collectionId}/update")
    public String update(@PathVariable("collectionId") long id, @RequestParam("name") String name,
            @RequestParam(name = "description", defaultValue = "") String description) {
        try { collections.update(id, viewer(), name, description); return "redirect:/collections/" + id; }
        catch (IllegalArgumentException e) { return Pages.error("/collections/" + id, e.getMessage()); }
        catch (DuplicateKeyException e) { return Pages.error("/collections/" + id, "You already have a collection with that name."); }
    }
    @PostMapping("/{collectionId}/delete")
    public String delete(@PathVariable("collectionId") long id) {
        collections.delete(id, viewer()); return "redirect:/collections";
    }
    // Fixed form action allows a native select to choose the destination collection.
    @PostMapping("/save")
    public String save(@RequestParam("collectionId") long id, @RequestParam("postId") long postId) {
        collections.add(id, viewer(), postId); return "redirect:/collections/" + id;
    }
    @PostMapping("/{collectionId}/posts")
    public String add(@PathVariable("collectionId") long id, @RequestParam("postId") long postId) {
        collections.add(id, viewer(), postId); return "redirect:/collections/" + id;
    }
    @PostMapping("/{collectionId}/posts/{postId}/remove")
    public String remove(@PathVariable("collectionId") long id, @PathVariable("postId") long postId) {
        collections.remove(id, viewer(), postId); return "redirect:/collections/" + id;
    }
}
