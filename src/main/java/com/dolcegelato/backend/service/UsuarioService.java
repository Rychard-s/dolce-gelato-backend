package com.dolcegelato.backend.service;

import com.dolcegelato.backend.dto.LoginRequest;
import com.dolcegelato.backend.dto.LoginResponse;
import com.dolcegelato.backend.dto.RegistroRequest;
import com.dolcegelato.backend.dto.UsuarioRequest;
import com.dolcegelato.backend.exception.NoAutenticadoException;
import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.Rol;
import com.dolcegelato.backend.model.Usuario;
import com.dolcegelato.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ===================== LOGIN =====================

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .filter(Usuario::isActivo)
                .orElse(null);

        // Mismo mensaje si no existe o si la contraseña está mal (no damos pistas)
        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new NoAutenticadoException("Usuario o contraseña incorrectos");
        }
        return jwtService.generarToken(usuario);
    }

    // ===================== CLIENTES =====================

    // Registro público: siempre crea un CLIENTE (nunca admin ni cocina) y lo deja con sesión iniciada
    public LoginResponse registrar(RegistroRequest request) {
        String telefono = request.getTelefono().trim();
        if (usuarioRepository.existsByUsernameIgnoreCase(telefono)) {
            throw new ReglaNegocioException("Ya hay una cuenta con el celular " + telefono + ". Ingresa con tu contraseña");
        }
        Usuario cliente = new Usuario();
        cliente.setUsername(telefono); // el celular es su usuario
        cliente.setTelefono(telefono);
        cliente.setNombre(request.getNombre().trim());
        cliente.setRol(Rol.CLIENTE);
        cliente.setActivo(true);
        cliente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return jwtService.generarToken(usuarioRepository.save(cliente));
    }

    // Datos de la cuenta con sesión (para llenar solo el formulario del pedido)
    public Usuario perfil(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
    }

    // ===================== CRUD =====================

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    public Usuario crear(UsuarioRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ReglaNegocioException("Ya existe el usuario " + username);
        }
        if (esVacio(request.getPassword())) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear un usuario");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(request.getNombre().trim());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.isActivo());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        String username = request.getUsername().trim().toLowerCase();

        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ReglaNegocioException("Ya existe otro usuario " + username);
        }

        boolean dejaDeSerAdminActivo = esAdminActivo(usuario)
                && (request.getRol() != Rol.ADMIN || !request.isActivo());
        if (dejaDeSerAdminActivo) {
            validarQueQuedeOtroAdmin();
        }

        usuario.setUsername(username);
        usuario.setNombre(request.getNombre().trim());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.isActivo());
        if (!esVacio(request.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        if (esAdminActivo(usuario)) {
            validarQueQuedeOtroAdmin();
        }
        usuarioRepository.delete(usuario);
    }

    // ===================== APOYO =====================

    private boolean esAdminActivo(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN && usuario.isActivo();
    }

    private void validarQueQuedeOtroAdmin() {
        if (usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
        }
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}