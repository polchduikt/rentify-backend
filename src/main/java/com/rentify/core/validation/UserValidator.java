package com.rentify.core.validation;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.DeleteAccountRequestDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.exception.ApiValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final Validator validator;

    public void validateUpdateProfile(UpdateUserRequestDto request) {
        Set<String> errors = collectBeanErrors(request);

        if (request.firstName() == null && request.lastName() == null && request.phone() == null) {
            errors.add("At least one profile field must be provided");
        }

        throwIfAny(errors);
    }

    public void validateChangePassword(ChangePasswordRequestDto request) {
        Set<String> errors = collectBeanErrors(request);

        if (!Objects.equals(request.newPassword(), request.confirmPassword())) {
            errors.add("New password and confirmation do not match");
        }

        if (Objects.equals(request.newPassword(), request.currentPassword())) {
            errors.add("New password must be different from current password");
        }

        throwIfAny(errors);
    }

    public void validateDeleteAccount(DeleteAccountRequestDto request) {
        throwIfAny(collectBeanErrors(request));
    }

    private <T> Set<String> collectBeanErrors(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        Set<String> errors = new LinkedHashSet<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return errors;
    }

    private void throwIfAny(Set<String> errors) {
        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }
}
