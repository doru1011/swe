package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;

@ApplicationException(rollback = true)			//TODO ArtikelValidation
public class InvalidArtikelNameException extends Exception {

private static final long serialVersionUID = -8973151010781329074L;
	
	private final String artikelname;
	
	public InvalidArtikelNameException(String artikelname, Collection<ConstraintViolation<Artikel>> violations) {
//		super(violations);
		this.artikelname = artikelname;
	}

	public String getArtikelName() {
		return artikelname;
	}
}
