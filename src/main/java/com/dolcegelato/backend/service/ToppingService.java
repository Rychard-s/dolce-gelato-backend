package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.TipoTopping;
import com.dolcegelato.backend.model.Topping;
import com.dolcegelato.backend.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToppingService {

    private final ToppingRepository toppingRepository;

    public List<Topping> listar(boolean soloDisponibles, TipoTopping tipo) {
        if (tipo != null) {
            return toppingRepository.findByTipoOrderByNombreAsc(tipo);
        }
        return soloDisponibles
                ? toppingRepository.findByDisponibleTrueOrderByTipoAscNombreAsc()
                : toppingRepository.findAllByOrderByTipoAscNombreAsc();
    }

    public Topping buscarPorId(Long id) {
        return toppingRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Topping no encontrado con id " + id));
    }

    public Topping crear(Topping topping) {
        topping.setId(null);
        if (toppingRepository.existsByNombreIgnoreCase(topping.getNombre())) {
            throw new ReglaNegocioException("Ya existe un topping con el nombre " + topping.getNombre());
        }
        return toppingRepository.save(topping);
    }

    public Topping actualizar(Long id, Topping datos) {
        Topping topping = buscarPorId(id);
        if (toppingRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otro topping con el nombre " + datos.getNombre());
        }
        topping.setNombre(datos.getNombre());
        topping.setTipo(datos.getTipo());
        topping.setPrecioExtra(datos.getPrecioExtra());
        topping.setDisponible(datos.isDisponible());
        return toppingRepository.save(topping);
    }

    // RN-02 / RF-08: marcar agotado o disponible
    public Topping cambiarDisponibilidad(Long id, boolean disponible) {
        Topping topping = buscarPorId(id);
        topping.setDisponible(disponible);
        return toppingRepository.save(topping);
    }

    public void eliminar(Long id) {
        Topping topping = buscarPorId(id);
        toppingRepository.delete(topping);
    }
}