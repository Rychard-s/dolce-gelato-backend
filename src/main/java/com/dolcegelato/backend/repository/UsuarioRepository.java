package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Rol;
import com.dolcegelato.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByUsernameAsc();

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    // Para no quedarnos sin administradores
    long countByRolAndActivoTrue(Rol rol);
}