package co.dilaser.nexo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetRequest {
    @NotBlank
    private String token;
    @NotBlank
    @Size(min = 10)
    private String newPassword;

    public ResetRequest() {}

}