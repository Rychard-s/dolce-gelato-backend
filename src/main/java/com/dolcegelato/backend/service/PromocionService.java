package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Producto;
import com.dolcegelato.backend.model.Promocion;
import com.dolcegelato.backend.model.TipoPromocion;
import com.dolcegelato.backend.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoService productoService;

    public List<Promocion> listar() {
        return promocionRepository.findAllByOrderByFechaInicioDesc();
    }

    // RF-15: las que se muestran en el inicio de la web
    public List<Promocion> listarVigentes() {
        return promocionRepository.listarVigentes(LocalDate.now(ZoneId.of("America/Lima")));
    }

    public Promocion buscarPorId(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción no encontrada con id " + id));
    }

    public Promocion crear(Promocion promocion) {
        promocion.setId(null);
        promocion.setProducto(obtenerProducto(promocion));
        validar(promocion);
        return promocionRepository.save(promocion);
    }

    public Promocion actualizar(Long id, Promocion datos) {
        Promocion promocion = buscarPorId(id);
        promocion.setTitulo(datos.getTitulo());
        promocion.setDescripcion(datos.getDescripcion());
        promocion.setTipo(datos.getTipo());
        promocion.setValor(datos.getValor());
        promocion.setProducto(obtenerProducto(datos));
        promocion.setFechaInicio(datos.getFechaInicio());
        promocion.setFechaFin(datos.getFechaFin());
        promocion.setActiva(datos.isActiva());
        validar(promocion);
        return promocionRepository.save(promocion);
    }

    public Promocion cambiarEstado(Long id, boolean activa) {
        Promocion promocion = buscarPorId(id);
        promocion.setActiva(activa);
        return promocionRepository.save(promocion);
    }

    public void eliminar(Long id) {
        Promocion promocion = buscarPorId(id);
        promocionRepository.delete(promocion);
    }

    // ---- Métodos de apoyo ----

    // El producto es opcional: si viene, se busca el real en la BD
    private Producto obtenerProducto(Promocion promocion) {
        if (promocion.getProducto() == null || promocion.getProducto().getId() == null) {
            return null;
        }
        return productoService.buscarPorId(promocion.getProducto().getId());
    }

    private void validar(Promocion promocion) {
        if (promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (promocion.getTipo() == TipoPromocion.PORCENTAJE
                && promocion.getValor().compareTo(new BigDecimal("90")) > 0) {
            throw new ReglaNegocioException("El descuento no puede ser mayor a 90 %");
        }

        if (promocion.getTipo() == TipoPromocion.PRECIO_FIJO) {
            if (promocion.getProducto() == null) {
                throw new ReglaNegocioException("Una promoción de precio fijo debe indicar el producto");
            }
            if (promocion.getValor().compareTo(promocion.getProducto().getPrecio()) >= 0) {
                throw new ReglaNegocioException("El precio de promoción (S/ " + promocion.getValor()
                        + ") debe ser menor al precio normal (S/ " + promocion.getProducto().getPrecio() + ")");
            }
        }
    }
}