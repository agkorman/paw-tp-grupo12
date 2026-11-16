package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

@Controller
public class SpaController {
    private final ServletContext context;

    @Autowired
    public SpaController(final ServletContext context) { this.context = context; }

    @InitBinder
    public void initBinder(final WebDataBinder binder) { binder.registerCustomEditor(String.class, new StringTrimmerEditor(true)); }

    // Exclude assets so missing JS/CSS files return 404, never the SPA HTML.
    @RequestMapping(value = {"/app", "/app/", "/app/{path:(?!assets$).+}/**"}, method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<String> index(final HttpServletRequest request) throws IOException {
        try (InputStream stream = context.getResourceAsStream("/WEB-INF/spa/index.html")) {
            if (stream == null) return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).build();
            final String prefix = HtmlUtils.htmlEscape(request.getContextPath());
            final String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("__PAW_API_BASE_PATH__", prefix + "/api")
                    .replace("__PAW_SPA_BASE_PATH__", prefix + "/app");
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8)).body(html);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "redirect", required = false) final String redirect,
                        final HttpServletRequest request) {
        final String destination = LoginRedirectUtils.safeRedirect(redirect, request.getContextPath())
                .filter(path -> path.startsWith("/app/")).map(path -> path.substring(4)).orElse("/");
        return "redirect:/app/login?returnTo=" + URLEncoder.encode(destination, StandardCharsets.UTF_8);
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register() { return "redirect:/app/register"; }
}
