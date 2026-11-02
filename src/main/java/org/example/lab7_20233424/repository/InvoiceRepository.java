package org.example.lab7_20233424.repository;
import org.example.lab7_20233424.dto.InvoiceListDto;
import org.example.lab7_20233424.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    @Query(value = """
            SELECT i.id AS id,
                   i.type AS type,
                   DATE_FORMAT(i.date, '%d/%m/%Y') AS date,
                   c.name AS customerName,
                   c.document_type AS documentType,
                   c.document AS document
            FROM invoice i
            JOIN customer c ON c.id = i.customer_id
            ORDER BY i.id DESC
            """, nativeQuery = true)
    List<InvoiceListDto> listarPedidos();
}