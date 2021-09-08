package de.caritas.cob.userservice.api.service.statistic;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.statistic.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceTest {

  private static final String FIELD_NAME_STATISTICS_ENABLED = "statisticsEnabled";
  private static final String PAYLOAD = "payload";

  @InjectMocks
  private StatisticsService statisticsService;
  @Mock
  private AmqpTemplate amqpTemplate;
  @Mock
  Logger logger;

  private AssignSessionStatisticsEvent assignSessionStatisticsEvent;
  private EventType eventType = EventType.ASSIGN_SESSION;

  @Before
  public void setup() {
    assignSessionStatisticsEvent = Mockito.mock(AssignSessionStatisticsEvent.class);
    when(assignSessionStatisticsEvent.getEventType()).thenReturn(eventType);
    when(assignSessionStatisticsEvent.getPayload()).thenReturn(Optional.of(PAYLOAD));
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void fireEvent_Should_NotSendStatisticsMessage_WhenStatisticsIsDisabled() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED,
        false);
    statisticsService.fireEvent(assignSessionStatisticsEvent);
    verify(amqpTemplate, times(0)).convertAndSend(anyString(), anyString(), anyString());
  }

  @Test
  public void fireEvent_Should_SendStatisticsMessage_WhenStatisticsIsEnabled() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED,
        true);
    when(assignSessionStatisticsEvent.getEventType()).thenReturn(eventType);
    when(assignSessionStatisticsEvent.getPayload()).thenReturn(Optional.of(PAYLOAD));

    statisticsService.fireEvent(assignSessionStatisticsEvent);
    verify(amqpTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
  }

  @Test
  public void fireEvent_Should_LogWarning_WhenPayloadIsEmpty() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED,
        true);
    when(assignSessionStatisticsEvent.getPayload()).thenReturn(Optional.empty());
    statisticsService.fireEvent(assignSessionStatisticsEvent);
    verify(logger, times(1))
        .warn(anyString());
  }

  @Test
  public void fireEvent_Should_UseEventTypeAsTopicAndSendPayloadOfEvent() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED,
        true);
    statisticsService.fireEvent(assignSessionStatisticsEvent);
    verify(amqpTemplate, times(1))
        .convertAndSend(anyString(), eq(eventType.toString()), eq(PAYLOAD));
  }

}
