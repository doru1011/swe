package de.shop.artikelverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ArtikelIdExistsException extends Exception {
	private static final long serialVersionUID = 402812193476257397L;

	private final Long artikelId;
	
	public ArtikelIdExistsException(Long artikelId) {
		super("Die Id " + artikelId + " existiert bereits");
		this.artikelId = artikelId;
	}

	public Long getId() {
		return artikelId;
	}
}
