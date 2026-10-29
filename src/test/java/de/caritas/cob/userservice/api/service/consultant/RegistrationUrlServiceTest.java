package de.caritas.cob.userservice.api.service.consultant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** unit tests for the agency part of {@link RegistrationUrlService}. */
@RunWith(MockitoJUnitRunner.class)
public class RegistrationUrlServiceTest {

  private static final String CONSULTANT_ID = "aadc0ecf-c048-4bfc-857d-8c9b2e425500";
  private static final Long AGENCY_ID = 98L;
  private static final String VALID_URL = "https://www.diakonie.de/registration/agency";
  private static final String INVALID_URL = "https://evil.example.com/registration";

  @InjectMocks private RegistrationUrlService underTest;

  @Mock private AuthenticatedUser authenticatedUser;
  @Mock private ConsultantService consultantService;
  @Mock private AgencyService agencyService;

  private Consultant mockAuthenticatedConsultant() {
    var consultant = Mockito.mock(Consultant.class);
    Mockito.lenient().when(consultant.getId()).thenReturn(CONSULTANT_ID);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(consultantService.getConsultant(CONSULTANT_ID)).thenReturn(Optional.of(consultant));
    return consultant;
  }

  @Test
  public void setAgencyRegistrationUrl_Should_delegateToAgencyService_When_memberAndValid() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(true);

    underTest.setAgencyRegistrationUrl(AGENCY_ID, VALID_URL);

    verify(agencyService).setAgencyRegistrationUrl(AGENCY_ID, VALID_URL, CONSULTANT_ID);
  }

  @Test
  public void setAgencyRegistrationUrl_Should_clearOverride_When_blank() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(true);

    underTest.setAgencyRegistrationUrl(AGENCY_ID, "  ");

    verify(agencyService).setAgencyRegistrationUrl(eq(AGENCY_ID), isNull(), eq(CONSULTANT_ID));
  }

  @Test(expected = ForbiddenException.class)
  public void setAgencyRegistrationUrl_Should_throwForbidden_When_notMember() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(false);

    try {
      underTest.setAgencyRegistrationUrl(AGENCY_ID, VALID_URL);
    } finally {
      verify(agencyService, never()).setAgencyRegistrationUrl(any(), any(), any());
    }
  }

  @Test(expected = BadRequestException.class)
  public void setAgencyRegistrationUrl_Should_throwBadRequest_When_domainNotAllowed() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(true);

    try {
      underTest.setAgencyRegistrationUrl(AGENCY_ID, INVALID_URL);
    } finally {
      verify(agencyService, never()).setAgencyRegistrationUrl(any(), any(), any());
    }
  }

  @Test
  public void deleteAgencyRegistrationUrl_Should_forwardBlankUrlWithAttribution_When_member() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(true);

    underTest.deleteAgencyRegistrationUrl(AGENCY_ID);

    // removal is forwarded as null url so the agencyService keeps recording who removed it
    verify(agencyService).setAgencyRegistrationUrl(eq(AGENCY_ID), isNull(), eq(CONSULTANT_ID));
  }

  @Test(expected = ForbiddenException.class)
  public void deleteAgencyRegistrationUrl_Should_throwForbidden_When_notMember() {
    var consultant = mockAuthenticatedConsultant();
    when(consultant.isInAgency(AGENCY_ID)).thenReturn(false);

    try {
      underTest.deleteAgencyRegistrationUrl(AGENCY_ID);
    } finally {
      verify(agencyService, never()).setAgencyRegistrationUrl(any(), any(), any());
    }
  }
}
