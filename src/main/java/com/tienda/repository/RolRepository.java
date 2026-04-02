package com.tienda.repository;

import com.tienda.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    //Se crea al momento de crear un usuario que por defecto tend'ra el rol de "USER"
    public Optional<Rol> findByRol(String rol);

}
