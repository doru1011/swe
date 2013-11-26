package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Lieferant;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Lieferanten nicht korrekt sind
 */

public class InvalidLieferantException extends LieferantValidationException {
	private static final long serialVersionUID = 8070688979320854972L;
	private final Lieferant lieferant;
	
	public InvalidLieferantException(Lieferant lieferant,
			                     Collection<ConstraintViolation<Lieferant>> violations) {
		super(violations);
		this.lieferant = lieferant;
	}

	public Lieferant getlieferant() {
		return lieferant;
	}
}
