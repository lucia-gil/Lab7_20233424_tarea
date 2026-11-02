package org.example.lab7_20233424.controller;
import jakarta.validation.Valid;
import org.example.lab7_20233424.entity.Product;
import org.example.lab7_20233424.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

// Alternativa implementada para personalizar los errores de conversión: messages.properties (claves typeMismatch.product.price y typeMismatch.product.stock)
@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("productList", productRepository.findAll());
        return "product/list";
    }

    @GetMapping("/new")
    public String nuevo(@ModelAttribute("product") Product product) {
        return "product/form";
    }

    @GetMapping("/edit")
    public String editar(@ModelAttribute("product") Product product,
                         @RequestParam("id") int id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            Product encontrado = opt.get();
            product.setId(encontrado.getId());
            product.setName(encontrado.getName());
            product.setPrice(encontrado.getPrice());
            product.setStock(encontrado.getStock());
            return "product/form";
        } else {
            return "redirect:/product";
        }
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("product") @Valid Product product,
                          BindingResult bindingResult, Model model) {
        boolean hayErrores = bindingResult.hasErrors();

        String nombre = product.getName();
        if (nombre != null && !nombre.trim().isEmpty()) {
            Optional<Product> existente = productRepository.findByName(nombre);
            if (existente.isPresent()) {
                if (product.getId() == null || !existente.get().getId().equals(product.getId())) {
                    model.addAttribute("errorNombre", "Ya existe un producto con ese nombre");
                    hayErrores = true;
                }
            }
        }

        if (hayErrores) {
            return "product/form";
        }

        productRepository.save(product);
        return "redirect:/product";
    }

    @GetMapping("/delete")
    public String borrar(@RequestParam("id") int id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            productRepository.deleteById(id);
        }
        return "redirect:/product";
    }
}