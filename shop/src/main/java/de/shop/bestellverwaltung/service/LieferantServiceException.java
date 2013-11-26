package de.shop.bestellverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class LieferantServiceException extends RuntimeException {
	private static final long serialVersionUID = -2849585609393128387L;

	public LieferantServiceException(String msg) {
		super(msg);
	}
	
	public LieferantServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
