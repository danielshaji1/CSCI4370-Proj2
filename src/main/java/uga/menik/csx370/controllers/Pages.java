package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.web.servlet.ModelAndView;

/** Small helpers shared by page controllers. */
final class Pages {
    private Pages() {}
    static ModelAndView posts(String template, String title, List<?> posts, String error) {
        ModelAndView page = new ModelAndView(template);
        page.addObject("posts", posts);
        page.addObject("isNoContent", posts.isEmpty());
        page.addObject("pageTitle", title);
        page.addObject("errorMessage", error);
        return page;
    }
    static String error(String path, String message) {
        return "redirect:" + path + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
