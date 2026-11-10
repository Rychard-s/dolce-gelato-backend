package com.dolcegelato.backend.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

// Respuesta paginada con nombres claros (en vez de devolver el Page de Spring tal cual)
@Getter
@AllArgsConstructor
public class PaginaResponse<T> {
    private List<T> contenido;
    private int pagina;          // empieza en 0
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
