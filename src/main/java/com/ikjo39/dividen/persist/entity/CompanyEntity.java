package com.ikjo39.dividen.persist.entity;

import com.ikjo39.dividen.model.Company;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "Company")
@Getter
@ToString
@NoArgsConstructor
public class CompanyEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String ticker;
	private String name;

	public CompanyEntity(Company company) {
		this.ticker = company.getTicker();
		this.name = company.getName();
	}
}
