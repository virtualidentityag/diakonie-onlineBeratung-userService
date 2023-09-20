package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Adapter class to provide a {@link ConsultantCreationInput} based on a {@link
 * CreateConsultantDTO}.
 */
@RequiredArgsConstructor
public class CreateConsultantDTOCreationInputAdapter implements ConsultantCreationInput {

  private final @NonNull CreateConsultantDTO createConsultantDTO;

  /**
   * Provides the old id.
   *
   * @return always null here
   */
  @Override
  public Long getIdOld() {
    return null;
  }

  /**
   * Provides the user name.
   *
   * @return the user name
   */
  @Override
  public String getUserName() {
    return this.createConsultantDTO.getUsername();
  }

  /**
   * Provides the encoded user name.
   *
   * @return the encoded user name
   */
  @Override
  public String getEncodedUsername() {
    return new UsernameTranscoder().encodeUsername(createConsultantDTO.getUsername());
  }

  /**
   * Provides the first name.
   *
   * @return the first name
   */
  @Override
  public String getFirstName() {
    return this.createConsultantDTO.getFirstname();
  }

  /**
   * Provides the last name.
   *
   * @return the last name
   */
  @Override
  public String getLastName() {
    return this.createConsultantDTO.getLastname();
  }

  /**
   * Provides the email address.
   *
   * @return the email address
   */
  @Override
  public String getEmail() {
    return this.createConsultantDTO.getEmail();
  }

  /**
   * Provides the absent flag.
   *
   * @return the absent flag
   */
  @Override
  public boolean isAbsent() {
    return isTrue(this.createConsultantDTO.getAbsent());
  }

  /**
   * Provides the absence message.
   *
   * @return the absence message
   */
  @Override
  public String getAbsenceMessage() {
    return this.createConsultantDTO.getAbsenceMessage();
  }

  /**
   * Provides the team consultant flag.
   *
   * @return the team consultant flag
   */
  @Override
  public boolean isTeamConsultant() {
    return false;
  }

  /**
   * Provides the language formal flag.
   *
   * @return the language formal flag
   */
  @Override
  public boolean isLanguageFormal() {
    return isTrue(this.createConsultantDTO.getFormalLanguage());
  }

  /**
   * Provides the tenant id.
   *
   * @return the tenant id
   */
  @Override
  public Long getTenantId() {
    return this.createConsultantDTO.getTenantId();
  }
}
