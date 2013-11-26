package de.shop.artikelverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ArtikelServiceException extends RuntimeException {
	private static final long serialVersionUID = -2849585609393128387L;

	public ArtikelServiceException(String msg) {
		super(msg);
	}
	
	public ArtikelServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
