package com.example.sprintsight.dtos.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.time.LocalDate;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private String startField;
    private String endField;

    @Override
    public void initialize(ValidDateRange constraint) {
        this.startField = constraint.start();
        this.endField   = constraint.end();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) return true;

        try {
            Method startAccessor = obj.getClass().getMethod(startField);
            Method endAccessor   = obj.getClass().getMethod(endField);

            Object startValue = startAccessor.invoke(obj);
            Object endValue   = endAccessor.invoke(obj);

            if (!(startValue instanceof LocalDate start) || !(endValue instanceof LocalDate end)) {
                return true;
            }

            return end.isAfter(start);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ValidDateRange could not access fields " + startField + "/" + endField, e);
        }
    }
}
