package com.example.demo.context;

import org.springframework.transaction.support.TransactionTemplate;

public class TransactionTemplateProvider {
	public static TransactionTemplate getTransactionTemplate() {
		return ApplicationContextProvider.getApplicationContext().getBean(TransactionTemplate.class);
	}
}
