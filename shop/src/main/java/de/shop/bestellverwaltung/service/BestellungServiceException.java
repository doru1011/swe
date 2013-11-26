package de.shop.bestellverwaltung.service;


public class BestellungServiceException extends RuntimeException {
	private static final long serialVersionUID = -2849585609393128387L;

	public BestellungServiceException(String msg) {
		super(msg);
	}
	
	public BestellungServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
