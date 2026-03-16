package com.futureprograms.NexusAPI.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateConverter {
    private static final List<DateTimeFormatter> formatters = new ArrayList<>();
    
    static {
        // Agregar todos los formatos posibles
        formatters.add(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        formatters.add(DateTimeFormatter.ofPattern("d/M/yyyy")); // Formato español: 5/4/1968
        formatters.add(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // Formato con padding
        formatters.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        formatters.add(DateTimeFormatter.ofPattern("M/d/yyyy")); // Formato US: 4/5/1968
        formatters.add(DateTimeFormatter.ofPattern("MM/dd/yyyy")); // Formato US con padding
    }
    
    public static LocalDate convertToLocalDate(String dateString) throws DateTimeParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new DateTimeParseException("Date string is empty", dateString, 0);
        }
        
        // Intentar con cada formato
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateString.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Continuar con el siguiente formato
            }
        }
        
        // Si ningún formato funcionó, lanzar excepción
        throw new DateTimeParseException(
            "Unable to parse date '" + dateString + "'. Supported formats: yyyy-MM-dd, d/M/yyyy, M/d/yyyy",
            dateString,
            0
        );
    }
}
