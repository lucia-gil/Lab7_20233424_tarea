package org.example.lab7_20233424.controller;
import jakarta.validation.Valid;
import org.example.lab7_20233424.entity.Customer;
import org.example.lab7_20233424.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("customerList", customerRepository.findAll());
        return "customer/list";
    }

    @GetMapping("/new")
    public String nuevo(@ModelAttribute("customer") Customer customer) {
        return "customer/form";
    }

    @GetMapping("/edit")
    public String editar(@ModelAttribute("customer") Customer customer,
                         @RequestParam("id") int id) {
        Optional<Customer> opt = customerRepository.findById(id);
        if (opt.isPresent()) {
            Customer encontrado = opt.get();
            customer.setId(encontrado.getId());
            customer.setName(encontrado.getName());
            customer.setDocumentType(encontrado.getDocumentType());
            customer.setDocument(encontrado.getDocument());
            return "customer/form";
        } else {
            return "redirect:/customer";
        }
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("customer") @Valid Customer customer,
                          BindingResult bindingResult, Model model) {
        boolean hayErrores = bindingResult.hasErrors();

        String tipo = customer.getDocumentType();
        String documento = customer.getDocument();

        if (tipo != null && !tipo.isEmpty()) {
            if (!tipo.equals("DNI") && !tipo.equals("RUC")) {
                model.addAttribute("errorTipo", "El tipo de documento solo puede ser DNI o RUC");
                hayErrores = true;
            } else if (documento != null && !documento.isEmpty()) {
                if (tipo.equals("DNI") && documento.length() != 8) {
                    model.addAttribute("errorDocumento", "El DNI debe tener exactamente 8 dígitos");
                    hayErrores = true;
                }
                if (tipo.equals("RUC") && documento.length() != 11) {
                    model.addAttribute("errorDocumento", "El RUC debe tener exactamente 11 dígitos");
                    hayErrores = true;
                }
            }
        }

        if (documento != null && !documento.isEmpty()) {
            Optional<Customer> existente = customerRepository.findByDocument(documento);
            if (existente.isPresent()) {
                if (customer.getId() == null || !existente.get().getId().equals(customer.getId())) {
                    model.addAttribute("errorDocumento", "Ya existe un cliente registrado con ese documento");
                    hayErrores = true;
                }
            }
        }

        if (hayErrores) {
            return "customer/form";
        }

        customerRepository.save(customer);
        return "redirect:/customer";
    }

    @GetMapping("/delete")
    public String borrar(@RequestParam("id") int id) {
        Optional<Customer> opt = customerRepository.findById(id);
        if (opt.isPresent()) {
            customerRepository.deleteById(id);
        }
        return "redirect:/customer";
    }
}