package de.shop.bestellverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class BestellpositionIdExistsException extends BestellpositionServiceException {
	private static final long serialVersionUID = 4867667611097919943L;
	private final Long bestellpositionId;
	
	public BestellpositionIdExistsException(Long bestellpositionId) {
		super("Die Id " + bestellpositionId + " existiert bereits");
		this.bestellpositionId = bestellpositionId;
	}

	public Long getId() {
		return bestellpositionId;
	}
}
