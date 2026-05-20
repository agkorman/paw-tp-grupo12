package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CommunityController {

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
    }

    @RequestMapping(value = "/communities", method = RequestMethod.GET)
    public ModelAndView communitiesHub() {
        return new ModelAndView("communities.jsp");
    }

    @RequestMapping(value = "/communities/new", method = RequestMethod.GET)
    public ModelAndView createCommunity() {
        return new ModelAndView("community-create.jsp");
    }

    @RequestMapping(value = "/communities/{communitySlug}", method = RequestMethod.GET)
    public ModelAndView communityDetail(
        @PathVariable final String communitySlug
    ) {
        if (!"classics".equals(communitySlug)) {
            throw new ResourceNotFoundException("community not found");
        }
        return new ModelAndView("community-detail.jsp");
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}",
        method = RequestMethod.GET
    )
    public ModelAndView communityPostDetail(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug
    ) {
        if (
            !"classics".equals(communitySlug) || !"falcon-60".equals(postSlug)
        ) {
            throw new ResourceNotFoundException("community post not found");
        }
        return new ModelAndView("community-post-detail.jsp");
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/submit",
        method = RequestMethod.GET
    )
    public ModelAndView createCommunityPost(
        @PathVariable final String communitySlug
    ) {
        if (!"classics".equals(communitySlug)) {
            throw new ResourceNotFoundException("community not found");
        }
        return new ModelAndView("community-post-form.jsp");
    }
}
