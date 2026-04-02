package com.tienda.repository;

import com.tienda.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    public Optional<Usuario> findByUsernameAndActivoTrue(String username);

    //Recupera la lista completa de usuarios para usuarios activos
    public List<Usuario> findByActivoTrue();

    public Optional<Usuario> findByUsername(String username);

    //Para finalizar el proceso de registro de un nuevo usuario... Despues de enviarle el correo de verificacion
    public Optional<Usuario> findByUsernameAndPassword(String username, String Password);

    //Se usa para recuperar la contraseña
    public Optional<Usuario> findByUsernameOrCorreo(String username, String correo);

    //Se utiliza para saber si hay un registro con el username o el correo... Si existe no se puede crear
    public boolean existsByUsernameOrCorreo(String username, String correo);

}
