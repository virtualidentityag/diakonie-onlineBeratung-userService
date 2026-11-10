package de.caritas.cob.userservice.api.service.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.RegistrationUrlDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.TechnicalUserConfig;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.httpheader.HttpHeadersResolver;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

  private static final String ORIGIN_URL = "subdomain.onlineberatung.net";

  @InjectMocks AgencyService agencyService;

  @Mock AgencyControllerApi agencyControllerApi;

  @Mock AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @Mock TenantHeaderSupplier tenantHeaderSupplier;

  @Mock SecurityHeaderSupplier securityHeaderSupplier;

  @Mock ApiClient apiClient;

  @Mock IdentityClient identityClient;

  @Mock IdentityClientConfig identityClientConfig;

  private static final String TECHNICAL_TOKEN = "technical-access-token";

  @ParameterizedTest
  @NullAndEmptySource
  void getAgenciesFromAgencyService_Should_returnEmptyList_When_nullPassed(List<Long> emptyIds) {
    List<AgencyDTO> result = this.agencyService.getAgencies(emptyIds);
    assertThat(result).isEmpty();
  }

  @Test
  void getAgenciesFromAgencyService_Should_passTenantId() {
    TenantContext.setCurrentTenant(1L);
    TenantHeaderSupplier tenantHeaderSupplier = new TenantHeaderSupplier(new HttpHeadersResolver());
    ReflectionTestUtils.setField(tenantHeaderSupplier, "multitenancy", true);
    ReflectionTestUtils.setField(agencyService, "tenantHeaderSupplier", tenantHeaderSupplier);
    HttpHeaders headers = new HttpHeaders();
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(headers);
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    var agencyDTOS =
        Lists.newArrayList(
            new de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO());
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);
    when(this.agencyControllerApi.getAgenciesByIds(Lists.newArrayList(1L))).thenReturn(agencyDTOS);

    this.agencyService.getAgency(1L);

    assertThat(headers.get("tenantId").get(0)).isEqualTo("1");
    TenantContext.clear();
  }

  @Test
  void setAgencyRegistrationUrl_Should_callApiWithUrlAndAddedBy() {
    givenTechnicalUserLogin();
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);

    this.agencyService.setAgencyRegistrationUrl(
        98L, "https://www.diakonie.de/registration/agency", "consultant-uuid");

    var captor = org.mockito.ArgumentCaptor.forClass(RegistrationUrlDTO.class);
    verify(agencyControllerApi)
        .setAgencyRegistrationUrl(org.mockito.ArgumentMatchers.eq(98L), captor.capture());
    assertThat(captor.getValue().getRegistrationUrl())
        .isEqualTo("https://www.diakonie.de/registration/agency");
    assertThat(captor.getValue().getAddedBy()).isEqualTo("consultant-uuid");
  }

  @Test
  void setAgencyRegistrationUrl_Should_callApiWithNullUrl_When_removing() {
    givenTechnicalUserLogin();
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);

    this.agencyService.setAgencyRegistrationUrl(98L, null, "consultant-uuid");

    var captor = org.mockito.ArgumentCaptor.forClass(RegistrationUrlDTO.class);
    verify(agencyControllerApi)
        .setAgencyRegistrationUrl(org.mockito.ArgumentMatchers.eq(98L), captor.capture());
    assertThat(captor.getValue().getRegistrationUrl()).isNull();
    assertThat(captor.getValue().getAddedBy()).isEqualTo("consultant-uuid");
  }

  @Test
  void setAgencyRegistrationUrl_Should_sendTechnicalUserBearerToken() {
    givenTechnicalUserLogin();
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);

    this.agencyService.setAgencyRegistrationUrl(98L, null, "consultant-uuid");

    verify(identityClient).loginUser("technical", "technical-password");
    verify(apiClient).addDefaultHeader("Authorization", "Bearer " + TECHNICAL_TOKEN);
  }

  private void givenTechnicalUserLogin() {
    var techUser = new TechnicalUserConfig();
    techUser.setUsername("technical");
    techUser.setPassword("technical-password");
    when(identityClientConfig.getTechnicalUser()).thenReturn(techUser);
    var login = new KeycloakLoginResponseDTO();
    login.setAccessToken(TECHNICAL_TOKEN);
    when(identityClient.loginUser("technical", "technical-password")).thenReturn(login);
    var headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + TECHNICAL_TOKEN);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(TECHNICAL_TOKEN)).thenReturn(headers);
  }
}
