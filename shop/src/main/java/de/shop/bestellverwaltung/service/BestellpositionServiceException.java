package de.shop.bestellverwaltung.service;

public class BestellpositionServiceException extends RuntimeException {
	private static final long serialVersionUID = -2849585609393128387L;

	public BestellpositionServiceException(String msg) {
		super(msg);
	}
	
	public BestellpositionServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
