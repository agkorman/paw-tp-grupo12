package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private final UserService userService;

    @Autowired
    public UsersController(final UserService userService) {
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/users/search", method = RequestMethod.GET)
    public ModelAndView searchUsers(@RequestParam(value = "q", required = false) final String query,
                                    @RequestParam(value = "page", defaultValue = "1") final int page) {
        LOGGER.debug("rendering user search results page={} hasQuery={}", page, query != null && !query.isBlank());
        final Page<User> resultsPage = userService.searchUsers(query, page);

        final ModelAndView mav = new ModelAndView("users-search.jsp");
        mav.addObject("query", query == null ? "" : query);
        mav.addObject("results", resultsPage.getItems());
        mav.addObject("currentPage", resultsPage.getPageNumber());
        mav.addObject("totalPages", resultsPage.getTotalPages());
        mav.addObject("totalItems", resultsPage.getTotalItems());
        return mav;
    }
}
