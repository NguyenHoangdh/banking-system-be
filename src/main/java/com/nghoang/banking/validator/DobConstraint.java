package com.nghoang.banking.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD) //sẽ được apply ở đâu
@Retention(RetentionPolicy.RUNTIME) //annotation này sẽ được xử lý lúc nào (runtime, compiler time (lombok))
@Constraint( //class chịu trách nhiệm xử lý cho annotation
        validatedBy = {DobValidator.class}
)
//class này là khai báo thôi, class xử lý là class trong @constrain
public @interface DobConstraint {
    //3 property cơ bản của 1 annotation
    String message() default "Invalid date of birth";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    //property custom
    int min();
}
