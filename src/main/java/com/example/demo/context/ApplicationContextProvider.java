package com.example.demo.context;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class ApplicationContextProvider implements ApplicationContextAware {

	@Getter
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
		applicationContext = context;
	}
}
