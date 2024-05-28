package com.example.demo.enums;

import com.example.demo.context.ApplicationContextProvider;
import com.example.demo.context.TransactionTemplateProvider;
import com.example.demo.dao.ParameterDao;
import com.example.demo.models.dto.ParameterDTO;

public interface BaseEnum {
	String getCode();

	default ParameterDTO getParameterValue() {
		return TransactionTemplateProvider.getTransactionTemplate().execute(action ->
				new ParameterDTO(ApplicationContextProvider.getApplicationContext()
				.getBean(ParameterDao.class)
				.getParameterByCode(getCode())));

	}
}
