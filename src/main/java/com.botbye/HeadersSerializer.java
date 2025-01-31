package com.botbye;

import com.botbye.model.Headers;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;


public class HeadersSerializer extends JsonSerializer<Headers> {

    @Override
    public void serialize(
            Headers value,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {
        Map<String, String> result = value.getEntries()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> String.join(",", e.getValue())
                        ));

        gen.writeObject(result);
    }
}
