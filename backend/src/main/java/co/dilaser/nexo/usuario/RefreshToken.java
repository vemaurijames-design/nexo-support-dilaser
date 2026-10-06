package co.dilaser.nexo.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;
    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;
    @Column(nullable = false)
    private boolean revocado = false;
    @Column(name = "user_agent")
    private String userAgent;
    private String ip;
}
