package com.rentify.core.validation;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class UserValidator extends AbstractValidator {

    public UserValidator(Validator validator) {
        super(validator);
    }

    public void validateUpdateProfile(UpdateUserRequestDto request) {
        Map<String, String> errors = collectBeanErrors(request);

        if (request.firstName() == null && request.lastName() == null && request.phone() == null) {
            errors.put("global", "At least one profile field must be provided");
        }

        throwIfAny(errors);
    }

    public void validateChangePassword(ChangePasswordRequestDto request) {
        Map<String, String> errors = collectBeanErrors(request);

        if (!Objects.equals(request.newPassword(), request.confirmPassword())) {
            errors.put("confirmPassword", "New password and confirmation do not match");
        }

        if (Objects.equals(request.newPassword(), request.currentPassword())) {
            errors.put("newPassword", "New password must be different from current password");
        }

        throwIfAny(errors);
    }
}
