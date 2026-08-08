package com.centresportifets.athlets_backend.auth.token;

import com.centresportifets.athlets_backend.user.UserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "account_token")
public class AccountToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String token;

	@Column(nullable = false, length = 30)
	private String type;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	public boolean isUsed() {
		return usedAt != null;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}