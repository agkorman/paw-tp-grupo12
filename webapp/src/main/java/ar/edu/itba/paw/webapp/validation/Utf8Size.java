package ar.edu.itba.paw.webapp.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Utf8SizeValidator.class)
public @interface Utf8Size {
    String message() default "{validation.user.password.bytes}";
    int max();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
