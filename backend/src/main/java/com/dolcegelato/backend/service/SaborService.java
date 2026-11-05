package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Sabor;
import com.dolcegelato.backend.repository.SaborRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaborService {

    private final SaborRepository saborRepository;

    public List<Sabor> listar(boolean soloDisponibles) {
        return soloDisponibles
                ? saborRepository.findByDisponibleTrueOrderByNombreAsc()
                : saborRepository.findAllByOrderByNombreAsc();
    }

    public Sabor buscarPorId(Long id) {
        return saborRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sabor no encontrado con id " + id));
    }

    public Sabor crear(Sabor sabor) {
        sabor.setId(null);
        if (saborRepository.existsByNombreIgnoreCase(sabor.getNombre())) {
            throw new ReglaNegocioException("Ya existe un sabor con el nombre " + sabor.getNombre());
        }
        return saborRepository.save(sabor);
    }

    public Sabor actualizar(Long id, Sabor datos) {
        Sabor sabor = buscarPorId(id);
        if (saborRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otro sabor con el nombre " + datos.getNombre());
        }
        sabor.setNombre(datos.getNombre());
        sabor.setDisponible(datos.isDisponible());
        return saborRepository.save(sabor);
    }

    // RN-02 / RF-08: marcar agotado o disponible
    public Sabor cambiarDisponibilidad(Long id, boolean disponible) {
        Sabor sabor = buscarPorId(id);
        sabor.setDisponible(disponible);
        return saborRepository.save(sabor);
    }

    public void eliminar(Long id) {
        Sabor sabor = buscarPorId(id);
        saborRepository.delete(sabor);
    }
}