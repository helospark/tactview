package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.deserializer;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class TimelinePositionMapDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(final String key, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new TimelinePosition(new BigDecimal(key));
    }

}
