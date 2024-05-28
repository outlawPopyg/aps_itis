package com.example.demo.dao;

import com.example.demo.enums.ParametersEnum;
import com.example.demo.models.Parameters;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ParameterDaoImpl implements ParameterDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Parameters getParameterByCode(String code) {
		return entityManager.createQuery("from Parameters p where p.code = :code", Parameters.class)
				.setParameter("code", code).getSingleResult();
	}

}
