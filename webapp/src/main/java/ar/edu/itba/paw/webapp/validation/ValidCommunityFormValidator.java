package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.webapp.form.CommunityForm;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidCommunityFormValidator implements ConstraintValidator<ValidCommunityForm, CommunityForm> {

    private final CommunityService communityService;

    @Autowired
    public ValidCommunityFormValidator(final CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public boolean isValid(final CommunityForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        final Set<Short> selectedTopicIds = form.getSelectedTopicIds() == null
                ? Collections.emptySet()
                : form.getSelectedTopicIds().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (selectedTopicIds.isEmpty()) {
            return true;
        }

        final Set<Short> availableTopicIds = communityService.getAvailableTopics().stream()
                .map(CommunityTopic::getId)
                .collect(Collectors.toSet());
        if (availableTopicIds.containsAll(selectedTopicIds)) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{validation.community.topics.invalid}")
                .addPropertyNode("selectedTopicIds")
                .addConstraintViolation();
        return false;
    }
}
