package org.example.lab7_20233424.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class InvoiceForm {

    @NotBlank(message = "Seleccione el tipo de comprobante")
    private String type;

    @NotBlank(message = "La fecha es obligatoria")
    private String date;

    @NotNull(message = "Seleccione un cliente")
    private Integer customerId;

    private List<String> productIds;

    private List<String> quantities;
}