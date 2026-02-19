package com.tienda.controller;

import com.tienda.service.CategoriaService;
import com.tienda.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {

    private final CategoriaService categoriaService;
    private final ProductoService productoService;

    public IndexController(CategoriaService categoriaService, ProductoService productoService) {
        this.categoriaService = categoriaService;
        this.productoService = productoService;
    }

    @GetMapping("/")
    public String listado(Model model) {
        var productos = productoService.getProductos(true);
        model.addAttribute("productos", productos);
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/index";
    }

    @GetMapping("/consultas/{idCategoria}")
    public String listado(@PathVariable("idCategoria") Integer idCategoria, Model model) {
        var categoriaOpt = categoriaService.getCategoria(idCategoria);
        if(categoriaOpt.isEmpty()){
            model.addAttribute("productos", java.util.Collections.EMPTY_LIST);
        }else {
            var categoria = categoriaOpt.get();
            var productos = categoria.getProductos();
            model.addAttribute("productos", productos);
        }
        
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/index";

    }
}
