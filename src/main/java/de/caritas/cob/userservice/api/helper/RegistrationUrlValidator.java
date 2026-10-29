package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates the optional registration redirect URL. A value is only accepted when it is an absolute
 * {@code https} URL without user-info whose host is exactly {@value #ALLOWED_REGISTRATION_DOMAIN}
 * or a subdomain of it. This rejects look-alike bypasses such as {@code
 * https://diakonie.de.evil.com}, {@code https://evil-diakonie.de}, {@code
 * https://evil.com/diakonie.de} and {@code https://diakonie.de@evil.com}.
 */
public final class RegistrationUrlValidator {

  public static final String ALLOWED_REGISTRATION_DOMAIN = "diakonie.de";

  private RegistrationUrlValidator() {}

  public static boolean isValid(String registrationUrl) {
    if (registrationUrl == null || registrationUrl.isBlank()) {
      return false;
    }
    final URI uri;
    try {
      uri = new URI(registrationUrl.trim());
    } catch (URISyntaxException e) {
      return false;
    }
    if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getRawUserInfo() != null) {
      return false;
    }
    var host = uri.getHost();
    if (host == null) {
      return false;
    }
    host = host.toLowerCase();
    return host.equals(ALLOWED_REGISTRATION_DOMAIN)
        || host.endsWith("." + ALLOWED_REGISTRATION_DOMAIN);
  }

  public static void validate(String registrationUrl) {
    if (!isValid(registrationUrl)) {
      throw new BadRequestException(
          String.format(
              "Registration url must be an https URL on the domain %s",
              ALLOWED_REGISTRATION_DOMAIN));
    }
  }
}
