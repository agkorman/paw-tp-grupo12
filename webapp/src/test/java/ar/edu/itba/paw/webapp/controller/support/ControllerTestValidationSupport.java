package ar.edu.itba.paw.webapp.controller.support;

import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.webapp.validation.ValidCarFormValidator;
import ar.edu.itba.paw.webapp.validation.ValidCommunityFormValidator;
import ar.edu.itba.paw.webapp.validation.ValidRecommendationFormValidator;
import ar.edu.itba.paw.webapp.validation.ValidReviewFormValidator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * Builds {@link SpringValidatorAdapter} for standalone {@link org.springframework.test.web.servlet.MockMvc},
 * injecting custom-validators that rely on collaborator beans.
 */
public final class ControllerTestValidationSupport {

    private ControllerTestValidationSupport() {}

    public static SpringValidatorAdapter carFormSpringValidator(final BrandService brandService,
                                                                final BodyTypeService bodyTypeService,
                                                                final CarService carService,
                                                                final CarRequestService carRequestService) {
        return buildDelegating(new SpecialFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends ConstraintValidator<?, ?>> T create(final Class<T> key) {
                if (ValidCarFormValidator.class.equals(key)) {
                    return (T) new ValidCarFormValidator(brandService, bodyTypeService, carService, carRequestService);
                }
                return null;
            }
        });
    }

    public static SpringValidatorAdapter reviewFormSpringValidator(final ReviewTagService reviewTagService) {
        return buildDelegating(new SpecialFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends ConstraintValidator<?, ?>> T create(final Class<T> key) {
                if (ValidReviewFormValidator.class.equals(key)) {
                    return (T) new ValidReviewFormValidator(reviewTagService);
                }
                return null;
            }
        });
    }

    public static SpringValidatorAdapter communityFormSpringValidator(final CommunityService communityService) {
        return buildDelegating(new SpecialFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends ConstraintValidator<?, ?>> T create(final Class<T> key) {
                if (ValidCommunityFormValidator.class.equals(key)) {
                    return (T) new ValidCommunityFormValidator(communityService);
                }
                return null;
            }
        });
    }

    public static SpringValidatorAdapter recommendationFormSpringValidator(final BodyTypeService bodyTypeService) {
        return buildDelegating(new SpecialFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends ConstraintValidator<?, ?>> T create(final Class<T> key) {
                if (ValidRecommendationFormValidator.class.equals(key)) {
                    return (T) new ValidRecommendationFormValidator(bodyTypeService);
                }
                return null;
            }
        });
    }

    private static SpringValidatorAdapter buildDelegating(final SpecialFactory special) {
        final ValidatorFactory bootstrap = Validation.buildDefaultValidatorFactory();
        final ConstraintValidatorFactory baseDelegate = bootstrap.getConstraintValidatorFactory();

        final ConstraintValidatorFactory delegating = new ConstraintValidatorFactory() {
            @Override
            public void releaseInstance(final ConstraintValidator<?, ?> instance) {
                if (instance instanceof ValidCarFormValidator
                        || instance instanceof ValidCommunityFormValidator
                        || instance instanceof ValidReviewFormValidator
                        || instance instanceof ValidRecommendationFormValidator) {
                    return;
                }
                baseDelegate.releaseInstance(instance);
            }

            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
                final T overridden = special.create(key);
                if (overridden != null) {
                    return overridden;
                }
                return baseDelegate.getInstance(key);
            }
        };

        final ValidatorFactory validatorFactory =
                Validation.byDefaultProvider().configure().constraintValidatorFactory(delegating).buildValidatorFactory();
        return new SpringValidatorAdapter(validatorFactory.getValidator());
    }

    private interface SpecialFactory {
        /** Return non-null only for validator classes instantiated manually. */
        <T extends ConstraintValidator<?, ?>> T create(Class<T> key);
    }
}
