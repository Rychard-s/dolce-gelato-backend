package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Categoria;
import com.dolcegelato.backend.model.Producto;
import com.dolcegelato.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dolcegelato.backend.repository.OpcionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
        private final OpcionRepository opcionRepository;

    public List<Producto> listar(Long categoriaId) {
        if (categoriaId != null) {
            return productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
        }
        return productoRepository.findAll();
    }

    public List<Producto> listarCarta() {
        return productoRepository.listarCarta();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        producto.setCategoria(obtenerCategoria(producto));
        validarHorario(producto);
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto producto = buscarPorId(id);
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setCategoria(obtenerCategoria(datos));
        producto.setHoraInicio(datos.getHoraInicio());
        producto.setHoraFin(datos.getHoraFin());
        producto.setCantidadSabores(datos.getCantidadSabores());
        producto.setToppingsLiquidos(datos.getToppingsLiquidos());
        producto.setToppingsSolidos(datos.getToppingsSolidos());
        producto.setAptoDelivery(datos.isAptoDelivery());
        producto.setActivo(datos.isActivo());
        validarHorario(producto);
        return productoRepository.save(producto);
    }

        public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (opcionRepository.existsByProductoId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el producto porque tiene opciones. Mejor desactívalo");
        }
        productoRepository.delete(producto);
    }

    // ---- Métodos de apoyo ----

    // Busca la categoría real en la BD (si no existe, lanza 404)
    private Categoria obtenerCategoria(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new ReglaNegocioException("Debe indicar el id de la categoría");
        }
        return categoriaService.buscarPorId(producto.getCategoria().getId());
    }

    // RN-01: la hora de fin debe ser posterior a la de inicio
    private void validarHorario(Producto producto) {
        if (!producto.getHoraFin().isAfter(producto.getHoraInicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }
}