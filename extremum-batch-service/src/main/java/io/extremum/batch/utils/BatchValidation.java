package io.extremum.batch.utils;

import io.extremum.batch.model.BatchRequestDto;
import io.extremum.batch.model.DataWithId;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

public class BatchValidation {
    public static Function<BatchRequestDto, Mono<Either<DataWithId, BatchRequestDto>>> validateRequest(Validator validator) {
        return requestDto -> Mono.fromCallable(() -> {
            Set<ConstraintViolation<BatchRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                return Either.left(new DataWithId(requestDto.getId(), violations.stream()
                        .map(v -> join(" - ", v.getPropertyPath().toString(), v.getMessage()))
                        .collect(joining(","))));
            }
            return Either.right(requestDto);
        });
    }
}
