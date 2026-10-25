package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.Carros;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrosRepository extends JpaRepository<Carros,Long> {
    boolean existsByIdAndVariacoesIsNotEmpty(Long carroId);
}
