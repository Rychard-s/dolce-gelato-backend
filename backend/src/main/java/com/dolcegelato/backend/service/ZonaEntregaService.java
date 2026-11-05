package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.ZonaEntrega;
import com.dolcegelato.backend.repository.ZonaEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaEntregaService {

    private final ZonaEntregaRepository zonaRepository;

    public List<ZonaEntrega> listar(boolean soloActivas) {
        return soloActivas
                ? zonaRepository.findByActivaTrueOrderByNombreAsc()
                : zonaRepository.findAllByOrderByNombreAsc();
    }

    public ZonaEntrega buscarPorId(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona de entrega no encontrada con id " + id));
    }

    public ZonaEntrega crear(ZonaEntrega zona) {
        zona.setId(null);
        if (zonaRepository.existsByNombreIgnoreCase(zona.getNombre())) {
            throw new ReglaNegocioException("Ya existe una zona con el nombre " + zona.getNombre());
        }
        return zonaRepository.save(zona);
    }

    public ZonaEntrega actualizar(Long id, ZonaEntrega datos) {
        ZonaEntrega zona = buscarPorId(id);
        if (zonaRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otra zona con el nombre " + datos.getNombre());
        }
        zona.setNombre(datos.getNombre());
        zona.setCostoEnvio(datos.getCostoEnvio());
        zona.setPedidoMinimo(datos.getPedidoMinimo());
        zona.setActiva(datos.isActiva());
        return zonaRepository.save(zona);
    }

    // RN-06: activar o desactivar una zona (ej. si un día no hay repartidor para esa zona)
    public ZonaEntrega cambiarEstado(Long id, boolean activa) {
        ZonaEntrega zona = buscarPorId(id);
        zona.setActiva(activa);
        return zonaRepository.save(zona);
    }

    public void eliminar(Long id) {
        ZonaEntrega zona = buscarPorId(id);
        zonaRepository.delete(zona);
    }
}