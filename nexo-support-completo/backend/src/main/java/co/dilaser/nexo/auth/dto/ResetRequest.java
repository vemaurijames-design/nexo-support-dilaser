package co.dilaser.nexo.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ResetRequest(@NotBlank String token, @NotBlank @Size(min = 10) String newPassword) {}
