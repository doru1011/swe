package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposition;

/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Kunden nicht korrekt sind
 */

public class InvalidBestellpositionException extends BestellpositionValidationException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Bestellposition bestellposition;
	
	public InvalidBestellpositionException(Bestellposition bestellposition, 
			Collection<ConstraintViolation<Bestellposition>> violations) {		
		super(violations);
		this.bestellposition = bestellposition;
	}

	public Bestellposition getbestellposition() {
		return bestellposition;
	}
}
