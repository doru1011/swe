package de.shop.bestellverwaltung.service;

import javax.ejb.ApplicationException;

import de.shop.bestellverwaltung.service.LieferantServiceException;

@ApplicationException(rollback = true)
public class LieferantenNameExistsException extends LieferantServiceException {

	private static final long serialVersionUID = 8770977657738687722L;
	private final String lieferantenname;
	
	public LieferantenNameExistsException(String lieferantenname) {
		super("Dieser Lieferant \"" + lieferantenname + "\" existiert bereits");
		this.lieferantenname = lieferantenname;
	}

	public String getArtikelname() {
		return lieferantenname;
	}

	
}
