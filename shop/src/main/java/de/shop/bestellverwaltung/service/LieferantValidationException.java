package de.shop.bestellverwaltung.service;

import java.util.Collection;
import javax.validation.ConstraintViolation;
import de.shop.bestellverwaltung.domain.Lieferant;


public class LieferantValidationException extends LieferantServiceException {
	private static final long serialVersionUID = -6924234959157503601L;
	private final Collection<ConstraintViolation<Lieferant>> violations;
	
	public LieferantValidationException(Collection<ConstraintViolation<Lieferant>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<Lieferant>> getViolations() {
		return violations;
	}
}
