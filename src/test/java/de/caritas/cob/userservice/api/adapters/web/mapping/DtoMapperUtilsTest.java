package de.caritas.cob.userservice.api.adapters.web.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import org.junit.jupiter.api.Test;

class DtoMapperUtilsTest {

  private final DtoMapperUtils dtoMapperUtils = new DtoMapperUtils() {};

  @Test
  void agencyResponseDtoOf_Should_mapRegistrationUrl() {
    var agencyDTO = new AgencyDTO();
    agencyDTO.setId(98L);
    agencyDTO.setRegistrationUrl("https://www.diakonie.de/registration/agency");

    var result = dtoMapperUtils.agencyResponseDtoOf(agencyDTO);

    assertThat(result.getId()).isEqualTo(98L);
    assertThat(result.getRegistrationUrl())
        .isEqualTo("https://www.diakonie.de/registration/agency");
  }

  @Test
  void agencyResponseDtoOf_Should_mapNullRegistrationUrl_When_noneSet() {
    var agencyDTO = new AgencyDTO();
    agencyDTO.setId(98L);

    var result = dtoMapperUtils.agencyResponseDtoOf(agencyDTO);

    assertThat(result.getRegistrationUrl()).isNull();
  }
}
