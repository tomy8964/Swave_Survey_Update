package com.example.surveydocument.chatGPT.request;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class MessageSerializerTest {
    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    private MessageSerializer messageSerializer;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        messageSerializer = new MessageSerializer();
    }

    @Test
    void serializeMessageToJson() throws IOException {
        // Create a sample Message
        Message message = new Message("role", "content");

        // Create a StringWriter to capture the JSON output
        StringWriter writer = new StringWriter();
        when(jsonGenerator.getCodec()).thenReturn(new JsonFactory().getCodec());
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Serialize the Message to JSON
        messageSerializer.serialize(message, jsonGenerator, serializerProvider);
    }
}