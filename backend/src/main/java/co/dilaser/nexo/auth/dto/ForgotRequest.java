package co.dilaser.nexo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgotRequest {
    @Email
    @NotBlank
    private String email;

    public ForgotRequest() {}

}