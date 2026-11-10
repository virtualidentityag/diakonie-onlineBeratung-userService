package de.caritas.cob.userservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailNotificationsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceMapperTest {

  @InjectMocks private UserServiceMapper userServiceMapper;

  @Mock
  @SuppressWarnings("unused")
  private UsernameTranscoder usernameTranscoder;

  @Test
  void saveWalkThroughEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.getWalkThroughEnabled()).isTrue();
  }

  @Test
  void saveNotificationsEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    NotificationsSettingsDTO allActiveSettings =
        new NotificationsSettingsDTO()
            .appointmentNotificationEnabled(true)
            .reassignmentNotificationEnabled(true)
            .newChatMessageNotificationEnabled(true)
            .appointmentNotificationEnabled(true);
    EmailNotificationsDTO emailNotificationsDTO =
        new EmailNotificationsDTO().emailNotificationsEnabled(true).settings(allActiveSettings);
    requestData.put("emailNotifications", emailNotificationsDTO);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.isNotificationsEnabled()).isTrue();
    assertThat(consultant.getNotificationsSettings())
        .isEqualTo(
            "{\"initialEnquiryNotificationEnabled\":false,\"newChatMessageNotificationEnabled\":true,\"reassignmentNotificationEnabled\":true,\"appointmentNotificationEnabled\":true}");
  }

  private static final String VALID_URL = "https://www.diakonie.de/registration/max";

  @Test
  void consultantOf_Should_setRegistrationUrlAndAddedDate_When_urlIsValid() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("registrationUrl", VALID_URL);
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.getRegistrationUrl()).isEqualTo(VALID_URL);
    assertThat(consultant.getRegistrationUrlAddedDate()).isNotNull();
  }

  @Test
  void consultantOf_Should_throwBadRequest_When_domainNotAllowed() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("registrationUrl", "https://evil.example.com/registration");
    Consultant consultant = new Consultant();

    assertThrows(
        BadRequestException.class, () -> userServiceMapper.consultantOf(consultant, requestData));
  }

  @Test
  void consultantOf_Should_clearUrlButKeepAddedDate_When_removingExistingUrl() {
    Consultant consultant = new Consultant();
    consultant.setRegistrationUrl(VALID_URL);
    consultant.setRegistrationUrlAddedDate(LocalDateTime.of(2020, 1, 1, 0, 0));
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("registrationUrl", "");

    userServiceMapper.consultantOf(consultant, requestData);

    // url is cleared but the added-date is updated to the removal date, not nulled
    assertThat(consultant.getRegistrationUrl()).isNull();
    assertThat(consultant.getRegistrationUrlAddedDate()).isNotNull();
    assertThat(consultant.getRegistrationUrlAddedDate())
        .isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
  }

  @Test
  void consultantOf_Should_leaveAddedDateNull_When_removingUrlThatWasNeverSet() {
    Consultant consultant = new Consultant();
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("registrationUrl", "   ");

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.getRegistrationUrl()).isNull();
    assertThat(consultant.getRegistrationUrlAddedDate()).isNull();
  }

  @Test
  void consultantOf_Should_notChangeRegistrationUrl_When_propertyMissing() {
    Consultant consultant = new Consultant();
    consultant.setRegistrationUrl(VALID_URL);
    var addedDate = LocalDateTime.of(2020, 1, 1, 0, 0);
    consultant.setRegistrationUrlAddedDate(addedDate);
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.getRegistrationUrl()).isEqualTo(VALID_URL);
    assertThat(consultant.getRegistrationUrlAddedDate()).isEqualTo(addedDate);
  }

  @Test
  void e2eKeyOfShouldMapIfKeyExists() {
    var map = Map.of("e2eKey", "tmp." + RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isPresent();
    assertThat(map).containsEntry("e2eKey", e2eKey.get());
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyFormatIsWrong() {
    var map = Map.of("e2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isNotPresent();
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyDoesNotExist() {
    var map = Map.of("notE2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isNotPresent();
  }
}
