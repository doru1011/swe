package de.shop.artikelverwaltung.service;


import javax.ejb.ApplicationException;

import de.shop.artikelverwaltung.service.ArtikelServiceException;

@ApplicationException(rollback = true)
public class ArtikelNameExistsException extends ArtikelServiceException {
	
	private static final long serialVersionUID = 4867667611097919943L;
	private final String artikelname;
	
	public ArtikelNameExistsException(String artikelname) {
		super("Dieser Artikel \"" + artikelname + "\" existiert bereits");
		this.artikelname = artikelname;
	}

	public String getArtikelname() {
		return artikelname;
	}

}
