package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.webapp.form.RecommendationForm;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RecommendationFormValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationFormValidator.class);

    private final BodyTypeService bodyTypeService;

    @Autowired
    public RecommendationFormValidator(final BodyTypeService bodyTypeService) {
        this.bodyTypeService = bodyTypeService;
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return RecommendationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final RecommendationForm form = (RecommendationForm) target;
        final String bodyType = form.getBodyType();
        if (bodyType == null || bodyType.trim().isEmpty()) {
            return;
        }
        if (!bodyTypeService.existsByName(bodyType)) {
            LOGGER.warn("recommendation rejected: invalid body type name={}",
                    LogSanitizer.forLog(bodyType, LogSanitizer.MAX_LOG_NAME_CODE_POINTS));
            errors.rejectValue("bodyType", "recommend.bodyType.invalid");
        }
    }
}
