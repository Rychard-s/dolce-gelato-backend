package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// Rango de fechas para los reportes: desde el inicio de "desde" hasta el final de "hasta"
public record RangoFechas(LocalDate desde, LocalDate hasta) {

    private static final long MAXIMO_DIAS = 366;

    // Si falta alguna fecha se usa "hoy"
    public static RangoFechas de(LocalDate desde, LocalDate hasta, LocalDate hoy) {
        LocalDate inicio = desde != null ? desde : hoy;
        LocalDate fin = hasta != null ? hasta : hoy;
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) >= MAXIMO_DIAS) {
            throw new ReglaNegocioException("El rango del reporte no puede pasar de un año");
        }
        return new RangoFechas(inicio, fin);
    }

    public LocalDateTime inicio() {
        return desde.atStartOfDay();
    }

    // Exclusivo: el día siguiente a las 00:00
    public LocalDateTime fin() {
        return hasta.plusDays(1).atStartOfDay();
    }
}
