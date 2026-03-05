package com.tienda.repository;

import com.tienda.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    public List<Producto> findByActivoTrue();
    
    //Estas 3 funciones hacen exactamente lo mismo, pero son diferentes formas de hacer consultas SQL
    
    //1 Consulta Derivada para recuperar los productos de un rango de precios, ordenados ascendentemente
    public List<Producto> findByPrecioBetweenOrderByPrecioAsc(double precioInf, double precioSup);
    
    //2 Consulta JPQL para recuperar los productos de un rango de precios, ordenados ascendentemente
    @Query(value="SELECT p FROM Producto p WHERE p.precio BETWEEN :precioInf and :precioSup ORDER BY p.precio ASC")
    public List<Producto> consultaJPQL(double precioInf, double precioSup);
    
    //3 Consulta SQL para recuperar los productos de un rango de precios, ordenados ascendentemente
    @Query(nativeQuery=true,
            value="SELECT * FROM producto p WHERE p.precio BETWEEN :precioInf and :precioSup ORDER BY p.precio ASC")
    public List<Producto> consultaSQL(double precioInf, double precioSup);
    
}
