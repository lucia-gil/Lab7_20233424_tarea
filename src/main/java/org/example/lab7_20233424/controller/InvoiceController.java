package org.example.lab7_20233424.controller;
import jakarta.validation.Valid;
import org.example.lab7_20233424.dto.InvoiceForm;
import org.example.lab7_20233424.dto.InvoiceListDto;
import org.example.lab7_20233424.entity.Customer;
import org.example.lab7_20233424.entity.Invoice;
import org.example.lab7_20233424.entity.InvoiceDetail;
import org.example.lab7_20233424.entity.Product;
import org.example.lab7_20233424.repository.CustomerRepository;
import org.example.lab7_20233424.repository.InvoiceDetailRepository;
import org.example.lab7_20233424.repository.InvoiceRepository;
import org.example.lab7_20233424.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceController(InvoiceRepository invoiceRepository,
                             InvoiceDetailRepository invoiceDetailRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        List<InvoiceListDto> pedidos = invoiceRepository.listarPedidos();
        Map<Integer, Double> totales = new HashMap<>();

        for (InvoiceListDto pedido : pedidos) {
            double total = 0;
            for (InvoiceDetail detalle : invoiceDetailRepository.findByInvoiceId(pedido.getId())) {
                total = total + detalle.getSubtotal();
            }
            totales.put(pedido.getId(), Math.round(total * 100.0) / 100.0);
        }

        model.addAttribute("invoiceList", pedidos);
        model.addAttribute("totales", totales);
        return "invoice/list";
    }

    @GetMapping("/new")
    public String nuevo(@ModelAttribute("invoiceForm") InvoiceForm invoiceForm, Model model) {
        cargarListas(model);
        return "invoice/form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("invoiceForm") @Valid InvoiceForm invoiceForm,
                          BindingResult bindingResult, Model model) {
        boolean hayErrores = bindingResult.hasErrors();
        List<String> erroresDetalle = new ArrayList<>();
        List<InvoiceDetail> detalles = new ArrayList<>();
        LocalDate fecha = null;

        String tipo = invoiceForm.getType();
        boolean tipoValido = tipo != null && (tipo.equals("Factura") || tipo.equals("Boleta"));
        if (tipo != null && !tipo.isEmpty() && !tipoValido) {
            model.addAttribute("errorTipo", "El tipo de comprobante solo puede ser Factura o Boleta");
            hayErrores = true;
        }

        String textoFecha = invoiceForm.getDate();
        if (textoFecha != null && !textoFecha.isEmpty()) {
            try {
                fecha = LocalDate.parse(textoFecha);
                if (fecha.isAfter(LocalDate.now())) {
                    model.addAttribute("errorFecha", "La fecha no puede ser futura");
                    hayErrores = true;
                }
            } catch (DateTimeParseException e) {
                model.addAttribute("errorFecha", "La fecha no tiene un formato válido");
                hayErrores = true;
            }
        }

        Customer cliente = null;
        if (invoiceForm.getCustomerId() != null) {
            Optional<Customer> optCliente = customerRepository.findById(invoiceForm.getCustomerId());
            if (optCliente.isPresent()) {
                cliente = optCliente.get();
            } else {
                model.addAttribute("errorCliente", "El cliente seleccionado no existe");
                hayErrores = true;
            }
        }

        if (cliente != null && tipoValido) {
            if (tipo.equals("Factura") && !cliente.getDocumentType().equals("RUC")) {
                model.addAttribute("errorGlobal", "La factura solo se puede emitir a clientes con RUC");
                hayErrores = true;
            }
            if (tipo.equals("Boleta") && !cliente.getDocumentType().equals("DNI")) {
                model.addAttribute("errorGlobal", "La boleta solo se puede emitir a clientes con DNI");
                hayErrores = true;
            }
        }

        List<String> productIds = invoiceForm.getProductIds();
        List<String> quantities = invoiceForm.getQuantities();
        List<Integer> usados = new ArrayList<>();

        if (productIds != null && quantities != null) {
            for (int i = 0; i < productIds.size(); i++) {
                String textoProducto = productIds.get(i);
                String textoCantidad = "";
                if (i < quantities.size()) {
                    textoCantidad = quantities.get(i).trim();
                }
                int fila = i + 1;

                if (textoProducto.isEmpty() && textoCantidad.isEmpty()) {
                    continue;
                }
                if (textoProducto.isEmpty() || textoCantidad.isEmpty()) {
                    erroresDetalle.add("Fila " + fila + ": seleccione el producto y escriba la cantidad");
                    continue;
                }

                int productId;
                int cantidad;
                try {
                    productId = Integer.parseInt(textoProducto);
                    cantidad = Integer.parseInt(textoCantidad);
                } catch (NumberFormatException e) {
                    erroresDetalle.add("Fila " + fila + ": la cantidad debe ser un número entero");
                    continue;
                }

                if (cantidad <= 0) {
                    erroresDetalle.add("Fila " + fila + ": la cantidad debe ser mayor a 0");
                    continue;
                }

                Optional<Product> optProducto = productRepository.findById(productId);
                if (!optProducto.isPresent()) {
                    erroresDetalle.add("Fila " + fila + ": el producto no existe");
                    continue;
                }
                Product producto = optProducto.get();

                if (usados.contains(productId)) {
                    erroresDetalle.add("Fila " + fila + ": el producto " + producto.getName() + " ya fue agregado");
                    continue;
                }
                usados.add(productId);

                if (cantidad > producto.getStock()) {
                    erroresDetalle.add("Fila " + fila + ": la cantidad supera el stock disponible de "
                            + producto.getName() + " (" + producto.getStock() + ")");
                    continue;
                }

                InvoiceDetail detalle = new InvoiceDetail();
                detalle.setProductId(productId);
                detalle.setQuantity(cantidad);
                detalle.setPrice(producto.getPrice());
                detalle.setSubtotal(Math.round(cantidad * producto.getPrice() * 100.0) / 100.0);
                detalles.add(detalle);
            }
        }

        if (detalles.isEmpty() && erroresDetalle.isEmpty()) {
            erroresDetalle.add("Debe agregar al menos un producto");
        }
        if (!erroresDetalle.isEmpty()) {
            model.addAttribute("erroresDetalle", erroresDetalle);
            hayErrores = true;
        }

        if (hayErrores) {
            cargarListas(model);
            return "invoice/form";
        }

        Invoice invoice = new Invoice();
        invoice.setType(tipo);
        invoice.setDate(fecha);
        invoice.setCustomerId(cliente.getId());
        Invoice guardado = invoiceRepository.save(invoice);

        for (InvoiceDetail detalle : detalles) {
            detalle.setInvoiceId(guardado.getId());
            invoiceDetailRepository.save(detalle);
        }

        return "redirect:/invoice";
    }

    private void cargarListas(Model model) {
        model.addAttribute("customerList", customerRepository.findAll());
        model.addAttribute("productList", productRepository.findAll());
        model.addAttribute("filas", List.of(1, 2, 3, 4, 5));
    }
}