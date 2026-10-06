package co.dilaser.nexo.auth.dto;
import co.dilaser.nexo.usuario.RolUsuario;
import java.util.UUID;
public record LoginResponse(String accessToken, String refreshToken, UUID userId, String nombre, String email,
                            RolUsuario rol, boolean debeCambiarPass) {}
