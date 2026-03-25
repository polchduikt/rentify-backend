package com.rentify.core.validation;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class UserValidator extends AbstractValidator {

    public UserValidator(Validator validator) {
        super(validator);
    }

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
}
