package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parameters")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Parameters {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Parameters parent;

	@OneToMany(mappedBy = "parent")
	private Set<Parameters> children = new HashSet<>();

}
