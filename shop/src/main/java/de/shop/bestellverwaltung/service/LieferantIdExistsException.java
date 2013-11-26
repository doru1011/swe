package de.shop.bestellverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class LieferantIdExistsException extends LieferantServiceException {
	private static final long serialVersionUID = 4867667611097919943L;
	private final Long lieferantId;
	
	public LieferantIdExistsException(Long lieferantId) {
		super("Die Id " + lieferantId + " existiert bereits");
		this.lieferantId = lieferantId;
	}

	public Long getId() {
		return lieferantId;
	}
}
