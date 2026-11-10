package com.dolcegelato.backend.service;

import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.model.Categoria;
import com.dolcegelato.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.repository.ProductoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByOrdenAsc();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + id));
    }

    public Categoria crear(Categoria categoria) {
        categoria.setId(null); // evita que alguien sobrescriba un registro existente
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria categoria = buscarPorId(id); // si no existe, lanza 404
        categoria.setNombre(datos.getNombre());
        categoria.setLinea(datos.getLinea());
        categoria.setOrden(datos.getOrden());
        categoria.setMinutosPreparacion(datos.getMinutosPreparacion());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la categoría porque tiene productos");
        }
        categoriaRepository.delete(categoria);
    }
}