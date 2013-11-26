package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposition;

public class BestellpositionValidationException extends BestellpositionServiceException {
	private static final long serialVersionUID = -6924234959157503601L;
	private final Collection<ConstraintViolation<Bestellposition>> violations;
	
	public BestellpositionValidationException(Collection<ConstraintViolation<Bestellposition>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<Bestellposition>> getViolations() {
		return violations;
	}
}
