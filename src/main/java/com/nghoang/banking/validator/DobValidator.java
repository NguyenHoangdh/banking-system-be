package com.nghoang.banking.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int min;
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {//mỗi anotation chỉ nên xử lý 1 constrain nhất định => cũng giống nguyên tắc S => việc null hay ko nên để @notnull xử lý
            return true;
        }
        long years = ChronoUnit.YEARS.between(value, LocalDate.now());

        return years >= min;
    }

    @Override
    //dùng để get thông số của annotation đó
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }
}
