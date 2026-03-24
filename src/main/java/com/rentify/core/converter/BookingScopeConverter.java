package com.rentify.core.converter;

import com.rentify.core.enums.BookingScope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class BookingScopeConverter implements Converter<String, BookingScope> {

    @Override
    public BookingScope convert(String source) {
        if (source == null || source.isBlank()) {
            return BookingScope.GUEST;
        }
        return BookingScope.valueOf(source.trim().toUpperCase(Locale.ROOT));
    }
}
