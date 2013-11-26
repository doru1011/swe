package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Lieferant;

public class InvalidLieferantNameException extends LieferantValidationException {
	private static final long serialVersionUID = 7545375834495974248L;
	private final String name;
		
	public InvalidLieferantNameException(String name, Collection<ConstraintViolation<Lieferant>> violations) {
		super(violations);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}


