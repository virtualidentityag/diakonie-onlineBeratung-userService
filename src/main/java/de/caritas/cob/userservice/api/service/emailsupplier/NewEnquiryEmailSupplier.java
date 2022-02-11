package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_NEW_ENQUIRY_NOTIFICATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Supplier to provide mails to be sent when a new enquiry was created.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class NewEnquiryEmailSupplier implements EmailSupplier {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyService agencyService;
  @Value("${app.base.url}")
  private String applicationBaseUrl;

  private Session session;

  public void setCurrentContext(Session session) {
    this.session = session;
  }

  /**
   * Generates the enquiry notification mails sent to regarding consultants when a new enquiry has
   * been created.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    log.info("Generating emails for new enquiry");
    List<ConsultantAgency> consultantAgencyList =
        consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(session.getAgencyId());
    log.info("Retrieved consultant agency list ", consultantAgencyList);
    if (isEmpty(consultantAgencyList)) {
      return emptyList();
    }
    AgencyDTO agency = agencyService.getAgency(session.getAgencyId());
    log.info("Retrieved agency " + agency);
    return consultantAgencyList.stream()
        .filter(this::validConsultantAgency)
        .map(toEnquiryMailDTO(agency))
        .collect(Collectors.toList());
  }

  private Boolean validConsultantAgency(ConsultantAgency consultantAgency) {
    return nonNull(consultantAgency)
        && isNotBlank(consultantAgency.getConsultant().getEmail())
        && !consultantAgency.getConsultant().isAbsent();
  }

  private Function<ConsultantAgency, MailDTO> toEnquiryMailDTO(AgencyDTO agency) {
    return consultantAgency -> buildMailDtoForNewEnquiryNotificationConsultant(
        consultantAgency.getConsultant().getEmail(),
        consultantAgency.getConsultant().getFullName(),
        session.getPostcode(),
        agency.getName()
    );
  }

  private MailDTO buildMailDtoForNewEnquiryNotificationConsultant(String email, String name,
      String postCode, String agency) {
    return new MailDTO()
        .template(TEMPLATE_NEW_ENQUIRY_NOTIFICATION)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO().key("name").value(name),
            new TemplateDataDTO().key("plz").value(postCode),
            new TemplateDataDTO().key("beratungsstelle").value(agency),
            new TemplateDataDTO().key("url").value(applicationBaseUrl)));
  }

}
