package de.shop.artikelverwaltung.service;

import java.util.Collection;


import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;


import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
										//TODO ArtikelValidation
public class InvalidNameException extends Exception {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String name;
	
	public InvalidNameException(String name, Collection<ConstraintViolation<Artikel>> violations) {
//		super(violations);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
