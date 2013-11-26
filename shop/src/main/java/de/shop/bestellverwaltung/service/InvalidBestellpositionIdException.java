package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposition;

public class InvalidBestellpositionIdException extends BestellpositionValidationException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final Long bestellpositionId;
	
	public InvalidBestellpositionIdException(Long bestellpositionId, 
			Collection<ConstraintViolation<Bestellposition>> violations) {
		super(violations);
		this.bestellpositionId = bestellpositionId;
	}

	public Long getBestellpositionId() {
		return bestellpositionId;
	}
}
