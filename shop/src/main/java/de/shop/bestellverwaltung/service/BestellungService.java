package de.shop.bestellverwaltung.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

import static de.shop.util.Constants.KEINE_ID;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;

@RolesAllowed({"admin","mitarbeiter", "kunde"})
@SecurityDomain("shop")
@Log
public class BestellungService implements Serializable {
	private static final long serialVersionUID = -519454062519816252L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public enum FetchType {
		NUR_BESTELLUNG,
		MIT_BESTELLPOSITIONEN
	}
	
	@Inject
	@NeueBestellung
	private transient Event<Bestellung> event;
	
	@Inject
	private KundeService ks;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@Inject
	private transient EntityManager em;
	
	//TODO Brauch man diese Methode überhaupt?
	public Bestellung findBestellungById(Long id) {
		try {
		final Bestellung bestellung = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID,
                											Bestellung.class)
                						.setParameter(Bestellung.PARAM_ID, id)
                						.getSingleResult();
		return bestellung;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	public Bestellung findBestellungByIdMitBestellpositionen(Long id) {
		try {
			final Bestellung bestellung = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_BESTELLPOSITIONEN,
                                                              Bestellung.class)
                                            .setParameter(Bestellung.PARAM_ID, id)
					                        .getSingleResult();
			return bestellung;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	public Kunde findKundeByBestellungId(Long id) {
		try {
			final Kunde kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_BESTELLUNG_ID,Kunde.class)
								   .setParameter(Bestellung.PARAM_ID, id)
								   .getSingleResult();
			return kunde;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	public Bestellung createBestellung(Bestellung bestellung, Kunde kunde) {
		if (bestellung == null || kunde == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		if (!em.contains(kunde)) {
			kunde = ks.findKundeById(kunde.getId(), KundeService.FetchType.MIT_BESTELLUNGEN);
		}
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);
		
		// Vor dem Abspeichern IDs zuruecksetzen:
		// IDs koennten einen Wert != null haben, wenn sie durch einen Web Service uebertragen wurden
		bestellung.setId(KEINE_ID);
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bp.setId(KEINE_ID);
		}
		
		em.persist(bestellung);
		event.fire(bestellung);
		
		return bestellung;
	}

	public Bestellung createBestellung(Bestellung bestellung, String username) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		final Kunde kunde = ks.findKundeByUsername(username);
		return createBestellung(bestellung, kunde);
	}
	
	public Bestellung updateBestellung(Bestellung bestellung) {
		if (bestellung == null) {
			return null;
		}

		// Pruefung, ob die Bestellung schon existiert
		final Bestellung vorhandeneBestellung = findBestellungById(bestellung.getId());
		if (vorhandeneBestellung.getId() == null) {
			throw new NotFoundException("Bestellung mit der ID " + bestellung.getId() + " existiert nicht.");
		}
		
		em.merge(bestellung);
		return bestellung;
	}
	
	public List<Bestellung> findBestellungenByKundeId(Long id) {
		if (id == null) { return Collections.emptyList(); }
		final List<Bestellung> bestellungen = em
				.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDE_ID, Bestellung.class)
				.setParameter(Bestellung.PARAM_ID, id)
				.getResultList(); 
		
		return bestellungen;		
	}
}
