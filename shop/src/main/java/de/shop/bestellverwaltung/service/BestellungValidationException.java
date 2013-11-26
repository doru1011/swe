package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;

public class BestellungValidationException extends BestellungServiceException {
	private static final long serialVersionUID = -6924234959157503601L;
	private final Collection<ConstraintViolation<Bestellung>> violations;
	
	public BestellungValidationException(Collection<ConstraintViolation<Bestellung>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
