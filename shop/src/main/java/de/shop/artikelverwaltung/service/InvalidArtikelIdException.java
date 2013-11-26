package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;

@ApplicationException(rollback = true)			//TODO ArtikelValidation
public class InvalidArtikelIdException extends Exception {
private static final long serialVersionUID = -8973151010781329074L;
	
	private final Long artikelId;
	
	public InvalidArtikelIdException(Long artikelId, Collection<ConstraintViolation<Artikel>> violations) {
//		super(violations);
		this.artikelId = artikelId;
	}

	public Long getArtikelId() {
		return artikelId;
	}

}
