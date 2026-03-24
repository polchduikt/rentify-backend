package com.rentify.core.converter;

import com.rentify.core.enums.PropertyType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PropertyTypeConverter implements AttributeConverter<PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(PropertyType attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PropertyType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PropertyType.fromValue(dbData);
    }
}
