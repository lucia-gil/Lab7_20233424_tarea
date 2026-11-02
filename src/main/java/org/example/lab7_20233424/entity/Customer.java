package org.example.lab7_20233424.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "document_type")
    private String documentType;

    @NotBlank(message = "El numero de documento es obligatorio")
    @Digits(integer = 11, fraction = 0, message = "El documento debe contener solo dígitos (máximo 11)")
    @Column(name = "document")
    private String document;
}