package de.shop.bestellverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class LieferantNameExistsException extends LieferantServiceException {
	
	private static final long serialVersionUID = 4813399151437318360L;
	private final String lieferantname;
	
	public LieferantNameExistsException(String lieferantname) {
		super("Dieser Artikel \"" + lieferantname + "\" existiert bereits");
		this.lieferantname = lieferantname;
	}

	public String getLieferantname() {
		return lieferantname;
	}

}