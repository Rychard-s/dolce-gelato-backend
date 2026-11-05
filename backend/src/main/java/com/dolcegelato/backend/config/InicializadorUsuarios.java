package com.dolcegelato.backend.config;

import com.dolcegelato.backend.model.Rol;
import com.dolcegelato.backend.model.Usuario;
import com.dolcegelato.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Al arrancar: si no hay usuarios, crea uno admin y uno de cocina para poder entrar
@Component
@RequiredArgsConstructor
public class InicializadorUsuarios implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        crear("admin", "Administrador", "admin123", Rol.ADMIN);
        crear("cocina", "Cocina", "cocina123", Rol.COCINA);
        System.out.println(">>> Usuarios de prueba creados: admin/admin123 y cocina/cocina123");
    }

    private void crear(String username, String nombre, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}