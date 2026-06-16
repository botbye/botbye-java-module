package com.botbye.common.http;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Serializes {@link Headers} to a flat JSON object on the wire: keys lowercased, multi-value headers
 * comma-joined. Single-value headers are written as-is.
 */
public final class HeadersSerializer extends JsonSerializer<Headers> {
    @Override
    public void serialize(Headers value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        Map<String, List<String>> headers = value.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                List<String> values = entry.getValue();
                String joined = values == null || values.isEmpty()
                        ? ""
                        : values.size() == 1 ? values.get(0) : String.join(", ", values);
                gen.writeStringField(entry.getKey().toLowerCase(Locale.ROOT), joined);
            }
        }
        gen.writeEndObject();
    }
}
