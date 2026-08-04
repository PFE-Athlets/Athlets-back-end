package com.centresportifets.athlets_backend.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
public class EmailService {

	private static final long TOKEN_EXPIRATION_MARGIN_SECONDS = 60;

	private final RestClient restClient;
	private final boolean mailEnabled;
	private final String fromEmail;
	private final String tenantId;
	private final String clientId;
	private final String clientSecret;
	private final String graphBaseUrl;
	private final String graphScope;

	private String cachedAccessToken;
	private Instant cachedAccessTokenExpiration;

	public EmailService(
			RestClient.Builder restClientBuilder,
			@Value("${app.mail.enabled:false}") boolean mailEnabled,
			@Value("${app.mail.from:}") String fromEmail,
			@Value("${app.mail.azure.tenant-id:}") String tenantId,
			@Value("${app.mail.azure.client-id:}") String clientId,
			@Value("${app.mail.azure.client-secret:}") String clientSecret,
			@Value("${app.mail.graph.base-url:https://graph.microsoft.com}") String graphBaseUrl,
			@Value("${app.mail.graph.scope:https://graph.microsoft.com/.default}")
					String graphScope) {
		this.restClient = restClientBuilder.build();
		this.mailEnabled = mailEnabled;
		this.fromEmail = fromEmail;
		this.tenantId = tenantId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.graphBaseUrl = graphBaseUrl;
		this.graphScope = graphScope;
	}

	@PostConstruct
	void validateConfiguration() {
		if (!mailEnabled) {
			log.info("Microsoft Graph email sending is disabled.");
			return;
		}

		requireConfiguration(fromEmail, "app.mail.from");
		requireConfiguration(tenantId, "app.mail.azure.tenant-id");
		requireConfiguration(clientId, "app.mail.azure.client-id");
		requireConfiguration(clientSecret, "app.mail.azure.client-secret");

		log.info(
				"Microsoft Graph email sending is enabled for mailbox {}.",
				fromEmail);
	}

	/**
	 * Sends an account activation email.
	 *
	 * @param to recipient email
	 * @param activationLink account activation link
	 */
	public void sendActivationEmail(String to, String activationLink) {
		String subject = "Activation de votre compte AthlETS";

		String body =
				"""
				Bonjour,

				Votre compte AthlETS a été créé.

				Pour l'activer et choisir votre mot de passe, utilisez le lien suivant :

				%s

				Ce lien est valide pendant 72 heures.

				Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer ce message.

				L'équipe AthlETS
				"""
						.formatted(activationLink);

		sendEmail(to, subject, body);
	}

	/**
	 * Sends a password reset email.
	 *
	 * @param to recipient email
	 * @param resetLink password reset link
	 */
	public void sendPasswordResetEmail(String to, String resetLink) {
		String subject = "Réinitialisation de votre mot de passe AthlETS";

		String body =
				"""
				Bonjour,

				Une demande de réinitialisation de mot de passe a été effectuée pour votre compte AthlETS.

				Pour choisir un nouveau mot de passe, utilisez le lien suivant :

				%s

				Ce lien est valide pendant 1 heure.

				Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer ce message.

				L'équipe AthlETS
				"""
						.formatted(resetLink);

		sendEmail(to, subject, body);
	}

	/**
	 * Sends an email through Microsoft Graph.
	 *
	 * @param to recipient email
	 * @param subject email subject
	 * @param body email body
	 */
	private void sendEmail(String to, String subject, String body) {
		if (!mailEnabled) {
			log.info(
					"Email sending is disabled. Skipping email with subject '{}' for recipient {}.",
					subject,
					to);
			return;
		}

		validateEmailRequest(to, subject, body);

		String accessToken = getAccessToken();

		Map<String, Object> requestBody =
				Map.of(
						"message",
						Map.of(
								"subject",
								subject,
								"body",
								Map.of(
										"contentType",
										"Text",
										"content",
										body),
								"toRecipients",
								List.of(
										Map.of(
												"emailAddress",
												Map.of("address", to)))),
						"saveToSentItems",
						true);

		try {
			restClient
					.post()
					.uri(
							graphBaseUrl + "/v1.0/users/{sender}/sendMail",
							fromEmail)
					.contentType(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(accessToken))
					.body(requestBody)
					.retrieve()
					.toBodilessEntity();

			log.info(
					"Email with subject '{}' sent successfully to {}.",
					subject,
					to);
		} catch (RestClientResponseException exception) {
			log.error(
					"Microsoft Graph rejected the email request. Status: {}, response: {}",
					exception.getStatusCode()
                );

			throw new EmailSendingException(
					"Microsoft Graph a refusé l'envoi du courriel.",
					exception);
		} catch (RuntimeException exception) {
			log.error(
					"Unexpected error while sending email through Microsoft Graph.",
					exception);

			throw new EmailSendingException(
					"Impossible d'envoyer le courriel.",
					exception);
		}
	}

	/**
	 * Obtains and temporarily caches a Microsoft Graph access token.
	 *
	 * @return valid Microsoft Graph access token
	 */
	private synchronized String getAccessToken() {
		if (isCachedTokenValid()) {
			return cachedAccessToken;
		}

		String tokenUrl =
				"https://login.microsoftonline.com/"
						+ tenantId
						+ "/oauth2/v2.0/token";

		MultiValueMap<String, String> formData =
				new LinkedMultiValueMap<>();

		formData.add("client_id", clientId);
		formData.add("client_secret", clientSecret);
		formData.add("scope", graphScope);
		formData.add("grant_type", "client_credentials");

		try {
			TokenResponse tokenResponse =
					restClient
							.post()
							.uri(tokenUrl)
							.contentType(
									MediaType.APPLICATION_FORM_URLENCODED)
							.body(formData)
							.retrieve()
							.body(TokenResponse.class);

			if (tokenResponse == null
					|| tokenResponse.accessToken() == null
					|| tokenResponse.accessToken().isBlank()) {
				throw new EmailSendingException(
						"Microsoft Entra n'a retourné aucun jeton d'accès.");
			}

			cachedAccessToken = tokenResponse.accessToken();
			cachedAccessTokenExpiration =
					Instant.now()
							.plusSeconds(tokenResponse.expiresIn())
							.minusSeconds(
									TOKEN_EXPIRATION_MARGIN_SECONDS);

			return cachedAccessToken;
		} catch (RestClientResponseException exception) {
			log.error(
					"Microsoft Entra rejected the token request. Status: {}, response: {}",
					exception.getStatusCode()
				);

			throw new EmailSendingException(
					"Impossible d'obtenir un jeton Microsoft Graph.",
					exception);
		}
	}

	private boolean isCachedTokenValid() {
		return cachedAccessToken != null
				&& cachedAccessTokenExpiration != null
				&& Instant.now().isBefore(cachedAccessTokenExpiration);
	}

	private void validateEmailRequest(
			String to,
			String subject,
			String body) {
		if (to == null || to.isBlank()) {
			throw new IllegalArgumentException(
					"L'adresse courriel du destinataire est obligatoire.");
		}

		if (subject == null || subject.isBlank()) {
			throw new IllegalArgumentException(
					"Le sujet du courriel est obligatoire.");
		}

		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException(
					"Le contenu du courriel est obligatoire.");
		}
	}

	private void requireConfiguration(
			String value,
			String propertyName) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(
					"La propriété "
							+ propertyName
							+ " est obligatoire lorsque l'envoi de courriel est activé.");
		}
	}

	private record TokenResponse(
			@JsonProperty("access_token") String accessToken,
			@JsonProperty("expires_in") long expiresIn) {}
}