package com.centresportifets.athlets_backend.auth.dto;

import lombok.Data;

@Data
public class GenerateActivationTokenRequest {
	private String username;
}