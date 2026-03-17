package ar.edu.itba.paw.webapp.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// Para poder utilizar recursos estáticos {
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// }

@EnableWebMvc
// En tiempo de ejecución, va a revisar ese paquete, va a encontrar la clase y va
// a ejecutar le método definido en el controller en vez del método default
@ComponentScan(
    { "ar.edu.itba.paw.webapp.controller", "ar.edu.itba.paw.services" }
)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/images/**")
            .addResourceLocations("/images/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
    }
}
