package ar.edu.itba.paw.webapp.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidCarSearchCriteriaValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidCarSearchCriteria {
    String message() default "{validation.carSearch.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
