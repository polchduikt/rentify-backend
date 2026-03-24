package com.rentify.core.converter;

import com.rentify.core.enums.PropertyType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPropertyTypeConverter implements Converter<String, PropertyType> {

    @Override
    public PropertyType convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return PropertyType.fromValue(source);
    }
}
