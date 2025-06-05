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

import java.util.regex.Pattern;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetails;
import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetailsMixin;
import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.WebAuthenticationDetailsMixin;

public class CrowdSpringSessionConverterFactory {
    private ObjectMapper buildMapper(ClassLoader classLoader) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Pattern.compile(".*"))
                .build();
        mapper.addMixIn(CrowdUserDetails.class, CrowdUserDetailsMixin.class);
        mapper.addMixIn(WebAuthenticationDetails.class, WebAuthenticationDetailsMixin.class);
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public CrowdSerializingConverter buildSerializer(ClassLoader classLoader) {
        return new CrowdSerializingConverter(buildMapper(classLoader));
    }

    public CrowdDeserializingConverter buildDeserializer(ClassLoader classLoader) {
        return new CrowdDeserializingConverter(buildMapper(classLoader), classLoader);
    }
}

