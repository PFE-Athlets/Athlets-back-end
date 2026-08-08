package com.centresportifets.athlets_backend.auth.dto;

import lombok.Data;

@Data
public class ActivateAccountRequest {
	private String token;
	private String newPassword;
	private String confirmPassword;
}