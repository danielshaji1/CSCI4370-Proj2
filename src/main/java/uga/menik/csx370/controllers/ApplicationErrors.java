package uga.menik.csx370.controllers;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/** Log actionable database diagnostics without exposing SQL details in a web page. */
@ControllerAdvice
public class ApplicationErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationErrors.class);
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ModelAndView database(Exception e) {
        LOG.error("Database operation failed", e);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "The database operation could not be completed. Please try again.");
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView status(ResponseStatusException e) {
        return error(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
    }
    private ModelAndView error(HttpStatus status, String message) {
        var page = new ModelAndView("error_page");
        page.setStatus(status);
        page.addObject("message", message);
        page.addObject("status", status.value());
        return page;
    }
}
