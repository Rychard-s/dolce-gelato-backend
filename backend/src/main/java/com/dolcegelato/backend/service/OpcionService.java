package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Opcion;
import com.dolcegelato.backend.model.Producto;
import com.dolcegelato.backend.repository.OpcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpcionService {

    private final OpcionRepository opcionRepository;
    private final ProductoService productoService;

    public List<Opcion> listar(Long productoId, boolean soloDisponibles) {
        if (productoId == null) {
            return opcionRepository.findAllByOrderByProductoIdAscGrupoAscNombreAsc();
        }
        return soloDisponibles
                ? opcionRepository.findByProductoIdAndDisponibleTrueOrderByGrupoAscNombreAsc(productoId)
                : opcionRepository.findByProductoIdOrderByGrupoAscNombreAsc(productoId);
    }

    public Opcion buscarPorId(Long id) {
        return opcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Opción no encontrada con id " + id));
    }

    public Opcion crear(Opcion opcion) {
        opcion.setId(null);
        opcion.setProducto(obtenerProducto(opcion));
        Long productoId = opcion.getProducto().getId();

        if (opcionRepository.existsByProductoIdAndGrupoIgnoreCaseAndNombreIgnoreCase(
                productoId, opcion.getGrupo(), opcion.getNombre())) {
            throw new ReglaNegocioException("Ya existe la opción " + opcion.getNombre()
                    + " en el grupo " + opcion.getGrupo() + " de este producto");
        }
        validarConfiguracionDelGrupo(opcion);
        return opcionRepository.save(opcion);
    }

    public Opcion actualizar(Long id, Opcion datos) {
        Opcion opcion = buscarPorId(id);
        Producto producto = obtenerProducto(datos);

        if (opcionRepository.existsByProductoIdAndGrupoIgnoreCaseAndNombreIgnoreCaseAndIdNot(
                producto.getId(), datos.getGrupo(), datos.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otra opción " + datos.getNombre()
                    + " en el grupo " + datos.getGrupo() + " de este producto");
        }
        opcion.setProducto(producto);
        opcion.setGrupo(datos.getGrupo());
        opcion.setNombre(datos.getNombre());
        opcion.setObligatorio(datos.isObligatorio());
        opcion.setMaxElegir(datos.getMaxElegir());
        opcion.setDisponible(datos.isDisponible());
        validarConfiguracionDelGrupo(opcion);
        return opcionRepository.save(opcion);
    }

    public Opcion cambiarDisponibilidad(Long id, boolean disponible) {
        Opcion opcion = buscarPorId(id);
        opcion.setDisponible(disponible);
        return opcionRepository.save(opcion);
    }

    public void eliminar(Long id) {
        Opcion opcion = buscarPorId(id);
        opcionRepository.delete(opcion);
    }

    // ---- Métodos de apoyo ----

    private Producto obtenerProducto(Opcion opcion) {
        if (opcion.getProducto() == null || opcion.getProducto().getId() == null) {
            throw new ReglaNegocioException("Debe indicar el id del producto");
        }
        return productoService.buscarPorId(opcion.getProducto().getId());
    }

    // Todas las opciones de un mismo grupo deben tener igual "obligatorio" y "maxElegir".
    // Ej: si "Sabor" de Alitas es obligatorio con maxElegir 1, BBQ y Buffalo deben decir lo mismo.
    private void validarConfiguracionDelGrupo(Opcion opcion) {
        List<Opcion> mismoGrupo = opcionRepository.findByProductoIdAndGrupoIgnoreCase(
                opcion.getProducto().getId(), opcion.getGrupo());

        for (Opcion otra : mismoGrupo) {
            if (otra.getId().equals(opcion.getId())) {
                continue; // es la misma opción que estamos editando
            }
            if (otra.isObligatorio() != opcion.isObligatorio() || otra.getMaxElegir() != opcion.getMaxElegir()) {
                throw new ReglaNegocioException("El grupo " + otra.getGrupo() + " ya está configurado con obligatorio="
                        + otra.isObligatorio() + " y maxElegir=" + otra.getMaxElegir()
                        + ". Todas sus opciones deben tener la misma configuración");
            }
        }
    }
}