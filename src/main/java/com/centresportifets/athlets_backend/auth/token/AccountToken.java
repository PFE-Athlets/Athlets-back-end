package com.centresportifets.athlets_backend.auth.token;

import com.centresportifets.athlets_backend.auth.AuthUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

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
	@JoinColumn(name = "user_id")
	private AuthUser user;

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public LocalDateTime getUsedAt() {
		return usedAt;
	}

	public void setUsedAt(LocalDateTime usedAt) {
		this.usedAt = usedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public AuthUser getUser() {
		return user;
	}

	public void setUser(AuthUser user) {
		this.user = user;
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}