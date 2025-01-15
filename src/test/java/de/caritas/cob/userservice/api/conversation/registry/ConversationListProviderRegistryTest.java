package de.caritas.cob.userservice.api.conversation.registry;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.conversation.provider.AnonymousEnquiryConversationListProvider;
import de.caritas.cob.userservice.api.conversation.provider.ConversationListProvider;
import de.caritas.cob.userservice.api.conversation.provider.RegisteredEnquiryConversationListProvider;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class ConversationListProviderRegistryTest {

  @InjectMocks private ConversationListProviderRegistry conversationListProviderRegistry;

  @Mock private ApplicationContext applicationContext;

  @Before
  public void setup() {
    ConversationListProvider anonymous = mock(AnonymousEnquiryConversationListProvider.class);
    when(anonymous.providedType()).thenReturn(ANONYMOUS_ENQUIRY);
    ConversationListProvider registered = mock(RegisteredEnquiryConversationListProvider.class);
    when(registered.providedType()).thenReturn(REGISTERED_ENQUIRY);
    when(this.applicationContext.getBeansOfType(any()))
        .thenReturn(Map.of("anonymous", anonymous, "registered", registered));
  }

  @Test(expected = NullPointerException.class)
  public void findByConversationType_Should_throwNPE_When_registryIsNotInitialized() {
    this.conversationListProviderRegistry.findByConversationType(ANONYMOUS_ENQUIRY);
  }

  @Test
  public void
      findByConversationType_Should_returnAnonymousEnquiryProvider_When_requestedTypeIsAnonymouseEnquiry() {
    this.conversationListProviderRegistry.initializeConversationSuppliers();

    ConversationListProvider resultProvider =
        this.conversationListProviderRegistry.findByConversationType(ANONYMOUS_ENQUIRY);

    assertThat(resultProvider.providedType(), is(ANONYMOUS_ENQUIRY));
    assertThat(resultProvider.getClass(), is(AnonymousEnquiryConversationListProvider.class));
  }

  @Test
  public void
      findByConversationType_Should_returnregisteredEnquiryProvider_When_requestedTypeIsRegisteredEnquiry() {
    this.conversationListProviderRegistry.initializeConversationSuppliers();

    ConversationListProvider resultProvider =
        this.conversationListProviderRegistry.findByConversationType(REGISTERED_ENQUIRY);

    assertThat(resultProvider.providedType(), is(REGISTERED_ENQUIRY));
    assertThat(resultProvider.getClass(), is(RegisteredEnquiryConversationListProvider.class));
  }

  @Test(expected = NoSuchElementException.class)
  public void findByConversationType_Should_throwNoSuchElementException_When_requestedTypeIsNull() {
    this.conversationListProviderRegistry.initializeConversationSuppliers();

    this.conversationListProviderRegistry.findByConversationType(null);
  }
}
