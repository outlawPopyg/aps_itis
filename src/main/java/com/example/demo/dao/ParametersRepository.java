package com.example.demo.dao;

import com.example.demo.models.Parameters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametersRepository extends JpaRepository<Parameters, Long> {
}
