package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Kunden nicht korrekt sind
 */
public class InvalidKundeException extends KundeValidationException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Kunde kunde;
	
	public InvalidKundeException(Kunde kunde,
			                     Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.kunde = kunde;
	}

	public Kunde getKunde() {
		return kunde;
	}
}
