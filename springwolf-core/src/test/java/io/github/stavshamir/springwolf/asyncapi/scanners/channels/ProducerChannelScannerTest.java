package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.kafka.KafkaOperationBinding;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.stavshamir.springwolf.asyncapi.types.ProducerData;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.PayloadReference;
import io.github.stavshamir.springwolf.configuration.AsyncApiDocket;
import io.github.stavshamir.springwolf.schemas.DefaultSchemasService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;

import static io.github.stavshamir.springwolf.asyncapi.Constants.ONE_OF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ProducerChannelScanner.class, DefaultSchemasService.class})
public class ProducerChannelScannerTest {

    @Autowired
    private ProducerChannelScanner scanner;

    @MockBean
    private AsyncApiDocket asyncApiDocket;

    @Test
    public void allFieldsProducerData() {
        // Given a producer data with all fields set
        String channelName = "example-producer-topic-foo1";
        ProducerData producerData = ProducerData.builder()
                .channelName(channelName)
                .binding(ImmutableMap.of("kafka", new KafkaOperationBinding()))
                .payloadType(ExamplePayloadDto.class)
                .build();

        when(asyncApiDocket.getProducers()).thenReturn(ImmutableList.of(producerData));

        // When scanning for producers
        Map<String, ChannelItem> producerChannels = scanner.scan();

        // Then the channel should be created correctly
        assertThat(producerChannels)
                .containsKey(channelName);

        Operation operation = Operation.builder()
                .bindings(ImmutableMap.of("kafka", new KafkaOperationBinding()))
                .message(Message.builder()
                        .name(ExamplePayloadDto.class.getName())
                        .title(ExamplePayloadDto.class.getSimpleName())
                        .payload(PayloadReference.fromModelName(ExamplePayloadDto.class.getSimpleName()))
                        .build())
                .build();

        ChannelItem expectedChannel = ChannelItem.builder()
                .subscribe(operation)
                .build();

        assertThat(producerChannels.get(channelName))
                .isEqualTo(expectedChannel);
    }

    @Test
    public void missingFieldProducerData() {
        // Given a producer data with missing fields
        String channelName = "example-producer-topic-foo1";
        ProducerData producerData = ProducerData.builder()
                .channelName(channelName)
                .build();

        when(asyncApiDocket.getProducers()).thenReturn(ImmutableList.of(producerData));

        // When scanning for producers
        Map<String, ChannelItem> producerChannels = scanner.scan();

        // Then the channel is not created, and no exception is thrown
        assertThat(producerChannels).isEmpty();
    }

    @Test
    public void multipleProducersForSameTopic() {
        // Given a multiple ProducerData objects for the same topic
        String channelName = "example-producer-topic";

        ProducerData producerData1 = ProducerData.builder()
                .channelName(channelName)
                .binding(ImmutableMap.of("kafka", new KafkaOperationBinding()))
                .payloadType(ExamplePayloadDto.class)
                .build();

        ProducerData producerData2 = ProducerData.builder()
                .channelName(channelName)
                .binding(ImmutableMap.of("kafka", new KafkaOperationBinding()))
                .payloadType(AnotherExamplePayloadDto.class)
                .build();

        when(asyncApiDocket.getProducers()).thenReturn(ImmutableList.of(producerData1, producerData2));

        // When scanning for producers
        Map<String, ChannelItem> producerChannels = scanner.scan();

        // Then one channel is created for the ProducerData objects with multiple messages
        assertThat(producerChannels)
                .hasSize(1)
                .containsKey(channelName);

        Set<Message> messages = ImmutableSet.of(
                Message.builder()
                        .name(ExamplePayloadDto.class.getName())
                        .title(ExamplePayloadDto.class.getSimpleName())
                        .payload(PayloadReference.fromModelName(ExamplePayloadDto.class.getSimpleName()))
                        .build(),
                Message.builder()
                        .name(AnotherExamplePayloadDto.class.getName())
                        .title(AnotherExamplePayloadDto.class.getSimpleName())
                        .payload(PayloadReference.fromModelName(AnotherExamplePayloadDto.class.getSimpleName()))
                        .build()
        );

        Operation operation = Operation.builder()
                .bindings(ImmutableMap.of("kafka", new KafkaOperationBinding()))
                .message(ImmutableMap.of(ONE_OF, messages))
                .build();

        ChannelItem expectedChannel = ChannelItem.builder()
                .subscribe(operation)
                .build();

        assertThat(producerChannels.get(channelName))
                .isEqualTo(expectedChannel);
    }

    static class ExamplePayloadDto {
        private String foo;
    }

    static class AnotherExamplePayloadDto {
        private String bar;
    }

}