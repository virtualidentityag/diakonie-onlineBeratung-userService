package de.caritas.cob.userservice.api.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import org.junit.Test;

/** unit tests for {@link RegistrationUrlValidator}. */
public class RegistrationUrlValidatorTest {

  @Test
  public void isValid_Should_accept_When_domainOrSubdomainOverHttps() {
    assertTrue(RegistrationUrlValidator.isValid("https://diakonie.de"));
    assertTrue(RegistrationUrlValidator.isValid("https://diakonie.de/registration"));
    assertTrue(
        RegistrationUrlValidator.isValid("https://beratung.diakonie.de/registration?aid=1739"));
    assertTrue(RegistrationUrlValidator.isValid("HTTPS://WWW.DIAKONIE.DE/x"));
  }

  @Test
  public void isValid_Should_reject_When_lookAlikeBypassOrNoHttps() {
    var invalid =
        new String[] {
          // missing/other scheme
          "diakonie.de",
          "http://diakonie.de",
          // subdomain-suffix, prefix and path tricks
          "https://diakonie.de.something-bad.com",
          "https://something-bad-diakonie.de",
          "https://something-bad.com/diakonie.de",
          // user-info tricks
          "https://diakonie.de@something-bad.com",
          "https://diakonie.de:@something-bad.com",
          // blank
          "",
          "   "
        };
    for (var url : invalid) {
      assertFalse("expected invalid: " + url, RegistrationUrlValidator.isValid(url));
    }
  }

  @Test
  public void isValid_Should_reject_When_null() {
    assertFalse(RegistrationUrlValidator.isValid(null));
  }

  @Test
  public void validate_Should_notThrow_When_valid() {
    RegistrationUrlValidator.validate("https://diakonie.de/registration");
  }

  @Test(expected = BadRequestException.class)
  public void validate_Should_throwBadRequest_When_invalid() {
    RegistrationUrlValidator.validate("https://diakonie.de@something-bad.com");
  }
}
