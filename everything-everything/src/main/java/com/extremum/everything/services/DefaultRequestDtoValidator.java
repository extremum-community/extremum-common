package com.extremum.everything.services;

import com.extremum.common.dto.RequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Service
public class DefaultRequestDtoValidator implements RequestDtoValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestDtoValidator.class);

    @Override
    public <R extends RequestDto> Set<ConstraintViolation<R>> validate(R requestDto) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator.validate(requestDto);
    }
}
