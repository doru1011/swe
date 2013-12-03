package de.shop.kundenverwaltung.service;

import de.shop.kundenverwaltung.domain.Kunde;


/**
 * Exception, die ausgel&ouml;st wird, wenn ein Kunde gel&ouml;scht werden soll, aber mindestens eine Bestellung hat
 */
public class KundeDeleteBestellungException extends KundeServiceException {
	private static final long serialVersionUID = 2237194289969083093L;
	private final String kundeUsername;
	private final int anzahlBestellungen;
	private static final String MESSAGE_KEY = "kunde.deleteMitBestellung";
	
	public KundeDeleteBestellungException(Kunde kunde) {
		super("Kunde " + kunde.getUsername() + " kann nicht geloescht werden: "
			  + kunde.getBestellungen().size() + " Bestellung(en)");
		this.kundeUsername = kunde.getUsername();
		this.anzahlBestellungen = kunde.getBestellungen().size();
	}

	public String getKundeId() {
		return kundeUsername;
	}
	public int getAnzahlBestellungen() {
		return anzahlBestellungen;
	}

	@Override
	public String getMessageKey() {
		return MESSAGE_KEY;
	}
}
