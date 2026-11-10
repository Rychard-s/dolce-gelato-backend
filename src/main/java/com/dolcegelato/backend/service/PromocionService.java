package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Categoria;
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
    private final CategoriaService categoriaService;

    public List<Promocion> listar() {
        return promocionRepository.findAllByOrderByFechaInicioDesc();
    }

    // RF-15: las que valen hoy (por fechas y, si es semanal, por día de la semana)
    public List<Promocion> listarVigentes() {
        return promocionRepository.listarVigentes(LocalDate.now(ZoneId.of("America/Lima"))).stream()
                .filter(Promocion::isVigente)
                .toList();
    }

    public Promocion buscarPorId(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción no encontrada con id " + id));
    }

    public Promocion crear(Promocion promocion) {
        promocion.setId(null);
        promocion.setProducto(obtenerProducto(promocion));
        promocion.setCategoria(obtenerCategoria(promocion));
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
        promocion.setCategoria(obtenerCategoria(datos));
        promocion.setCantidad(datos.getCantidad());
        promocion.setDiaSemana(datos.getDiaSemana());
        promocion.setSoloEnTienda(datos.isSoloEnTienda());
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

    // El producto y la categoría son opcionales: si vienen, se buscan los reales en la BD
    private Producto obtenerProducto(Promocion promocion) {
        if (promocion.getProducto() == null || promocion.getProducto().getId() == null) {
            return null;
        }
        return productoService.buscarPorId(promocion.getProducto().getId());
    }

    private Categoria obtenerCategoria(Promocion promocion) {
        if (promocion.getCategoria() == null || promocion.getCategoria().getId() == null) {
            return null;
        }
        return categoriaService.buscarPorId(promocion.getCategoria().getId());
    }

    private void validar(Promocion promocion) {
        if (promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (promocion.getProducto() != null && promocion.getCategoria() != null) {
            throw new ReglaNegocioException("La promoción aplica a un producto o a una categoría, no a los dos");
        }

        switch (promocion.getTipo()) {
            case PORCENTAJE -> {
                if (promocion.getValor().compareTo(new BigDecimal("90")) > 0) {
                    throw new ReglaNegocioException("El descuento no puede ser mayor a 90 %");
                }
                promocion.setCantidad(null);
            }
            case PRECIO_FIJO -> {
                Producto producto = exigirProducto(promocion, "Una promoción de precio especial");
                if (promocion.getValor().compareTo(producto.getPrecio()) >= 0) {
                    throw new ReglaNegocioException("El precio de promoción (S/ " + promocion.getValor()
                            + ") debe ser menor al precio normal (S/ " + producto.getPrecio() + ")");
                }
                promocion.setCantidad(null);
            }
            case CANTIDAD -> {
                Producto producto = exigirProducto(promocion, "Una promoción por cantidad");
                if (promocion.getCantidad() == null || promocion.getCantidad() < 2) {
                    throw new ReglaNegocioException("Indique cuántas unidades forman la promoción (mínimo 2)");
                }
                BigDecimal precioNormal = producto.getPrecio().multiply(BigDecimal.valueOf(promocion.getCantidad()));
                if (promocion.getValor().compareTo(precioNormal) >= 0) {
                    throw new ReglaNegocioException("El precio de " + promocion.getCantidad() + " unidades (S/ "
                            + promocion.getValor() + ") debe ser menor al normal (S/ " + precioNormal + ")");
                }
            }
        }
    }

    private Producto exigirProducto(Promocion promocion, String descripcion) {
        if (promocion.getProducto() == null) {
            throw new ReglaNegocioException(descripcion + " debe indicar el producto");
        }
        return promocion.getProducto();
    }
}
