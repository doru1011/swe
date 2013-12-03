package de.shop.kundenverwaltung.service;

import de.shop.util.AbstractShopException;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class KundeServiceException extends AbstractShopException {
	private static final long serialVersionUID = 5999208465631860486L;

	public KundeServiceException(String msg) {
		super(msg);
	}

	public KundeServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
