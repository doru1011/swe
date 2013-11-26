package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Lieferant;

public class InvalidLieferantIdException extends LieferantValidationException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final Long lieferantId;
	
	public InvalidLieferantIdException(Long lieferantId, Collection<ConstraintViolation<Lieferant>> violations) {
		super(violations);
		this.lieferantId = lieferantId;
	}

	public Long getLieferantId() {
		return lieferantId;
	}
}
