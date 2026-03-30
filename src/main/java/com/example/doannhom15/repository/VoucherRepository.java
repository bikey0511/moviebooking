package com.example.doannhom15.repository;

import com.example.doannhom15.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCaseAndActiveTrue(String code);
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
