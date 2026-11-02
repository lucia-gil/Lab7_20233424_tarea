package org.example.lab7_20233424.repository;
import org.example.lab7_20233424.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {

    List<InvoiceDetail> findByInvoiceId(Integer invoiceId);
}