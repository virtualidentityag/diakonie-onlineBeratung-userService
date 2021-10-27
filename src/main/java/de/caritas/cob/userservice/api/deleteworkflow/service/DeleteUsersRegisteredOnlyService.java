package de.caritas.cob.userservice.api.deleteworkflow.service;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ALL;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.helper.DateCalculator;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of askers with no running sessions.
 */
@Service
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyService {

  private final @NonNull UserRepository userRepository;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;

  @Value("${user.registeredonly.deleteWorkflow.check.days}")
  private int userRegisteredOnlyDeleteWorkflowCheckDays;

  /**
   * Deletes all askers without running sessions.
   */
  public void deleteUserAccounts() {

    var workflowErrors = deleteAskersAndCollectPossibleErrors();

    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }

  }

  private List<DeletionWorkflowError> deleteAskersAndCollectPossibleErrors() {

    LocalDateTime dateTimeToCheck = DateCalculator
        .calculateDateInThePastAtMidnight(userRegisteredOnlyDeleteWorkflowCheckDays);
    return userRepository
        .findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateTimeToCheck)
        .stream()
        .map(this::performUserDeletion)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<DeletionWorkflowError> performUserDeletion(User user) {
    try {
      user.setRcUserId(rocketChatService.getRocketChatUserIdByUsername(user.getUsername()));
    } catch (RocketChatGetUserIdException ex) {
      return Collections.singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(ALL)
              .identifier(user.getUserId() + "/" + user.getUsername())
              .reason(ex.getMessage())
              .timestamp(nowInUtc())
              .build());
    }
    return deleteUserAccountService.performUserDeletion(user);
  }

}
