package com.example.demo.dao;

import com.example.demo.models.Parameters;

public interface ParameterDao {
	Parameters getParameterByCode(String code);

}
