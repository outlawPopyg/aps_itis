package com.example.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParametersEnum implements BaseEnum {
	CONTEXT("1"),
	FIELDS("2"),
	EXAMPLES("EXAMPLE"),
	MODEL("MODEL"),

	SMP("3"),
	POLICE("4"),

	FIO("5"),
	ADDRESS("6"),

	;

	private final String code;

}
