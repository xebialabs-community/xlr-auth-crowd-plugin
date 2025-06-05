/**
 * Copyright 2025 DIGITAL.AI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.xlrelease.plugins.security.crowd.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.security.core.context.SecurityContext;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class CrowdDeserializingConverter implements Converter<byte[], Object> {
    private final DeserializingConverter defaultDeserializingConverter;
    private final ObjectMapper mapper;

    public CrowdDeserializingConverter(ObjectMapper mapper, ClassLoader classLoader) {
        this.mapper = mapper;
        this.defaultDeserializingConverter = new DeserializingConverter(classLoader);
    }

    @Override
    public Object convert(byte[] byteArray) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
            InputStreamReader reader = new InputStreamReader(in);
            return mapper.readValue(reader, SecurityContext.class);
        } catch (Throwable t) {
            return defaultDeserializingConverter.convert(byteArray);
        }
    }
}

