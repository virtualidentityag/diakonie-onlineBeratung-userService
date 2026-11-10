package de.caritas.cob.userservice.api.service.consultant;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.RegistrationUrlValidator;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles the optional shared registration redirect URL of the agencies a consultant is assigned
 * to. The URL is persisted in the agencyService; the membership check is performed here because the
 * consultant-agency relation only exists in the userService.
 *
 * <p>The consultant's own personal registration redirect URL is handled through {@code PATCH
 * /users/data} (see {@code UserServiceMapper#consultantOf}) and is therefore not part of this
 * service.
 */
@Service
@RequiredArgsConstructor
public class RegistrationUrlService {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull AgencyService agencyService;

  /**
   * Sets/updates the shared registration redirect URL of an agency the authenticated consultant is
   * assigned to. A {@code null}/blank URL removes the override.
   */
  public void setAgencyRegistrationUrl(Long agencyId, String registrationUrl) {
    var consultant = getAuthenticatedConsultant();
    verifyAgencyMembership(consultant, agencyId);
    if (!isBlank(registrationUrl)) {
      RegistrationUrlValidator.validate(registrationUrl);
    }
    agencyService.setAgencyRegistrationUrl(
        agencyId, isBlank(registrationUrl) ? null : registrationUrl.trim(), consultant.getId());
  }

  /**
   * Removes the shared registration redirect URL of an agency the authenticated consultant is
   * assigned to. The removal is forwarded as an empty URL so the agencyService keeps recording who
   * removed it and when.
   */
  public void deleteAgencyRegistrationUrl(Long agencyId) {
    var consultant = getAuthenticatedConsultant();
    verifyAgencyMembership(consultant, agencyId);
    agencyService.setAgencyRegistrationUrl(agencyId, null, consultant.getId());
  }

  private Consultant getAuthenticatedConsultant() {
    var consultantId = authenticatedUser.getUserId();
    return consultantService
        .getConsultant(consultantId)
        .orElseThrow(() -> new NotFoundException("Consultant with id %s not found", consultantId));
  }

  private void verifyAgencyMembership(Consultant consultant, Long agencyId) {
    if (!consultant.isInAgency(agencyId)) {
      throw new ForbiddenException(
          String.format(
              "Consultant with id %s is not assigned to agency %s", consultant.getId(), agencyId));
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
