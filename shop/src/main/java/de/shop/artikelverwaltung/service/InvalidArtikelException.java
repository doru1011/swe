package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Artikel nicht korrekt sind
 */
@ApplicationException(rollback = true)          //TODO ArtikelValidationException ?!           
public class InvalidArtikelException extends Exception {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Artikel artikel;
	
	public InvalidArtikelException(Artikel artikel,
			                     Collection<ConstraintViolation<Artikel>> violations) {
//		super(violations);
		this.artikel = artikel;
	}

	public Artikel getKunde() {
		return artikel;
	}
}
