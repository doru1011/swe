package de.shop.kundenverwaltung.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import de.shop.auth.domain.RolleType;
import de.shop.auth.service.AuthService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.Log;
import de.shop.util.MimeType;
import de.shop.util.NoMimeTypeException;
import de.shop.util.NotFoundException;

@RolesAllowed({"mitarbeiter", "admin"})
@SecurityDomain("shop")
@Log
public class KundeService implements Serializable {
	private static final long serialVersionUID = 3188789767052580247L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public enum FetchType {
		NUR_KUNDE,
		MIT_BESTELLUNGEN
	}
	
	public enum OrderByType {
		UNORDERED,
		USERNAME
	}
	
	@Inject
	private transient EntityManager em;
	
	@Inject
	private transient Event<Kunde> event;
	
	@Inject
	private FileHelper fileHelper;
	
	@Inject
	private AuthService authService;
	
	@Inject
	private transient ManagedExecutorService managedExecutorService;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	public Kunde findKundeById(Long id, FetchType fetch) {
		Kunde kunde = null;
		try {
			switch (fetch) {
				case NUR_KUNDE:
					kunde = em.find(Kunde.class, id);
					break;
				
				case MIT_BESTELLUNGEN:
					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN, Kunde.class)
							  .setParameter(Kunde.PARAM_KUNDE_ID, id)
							  .getSingleResult();
					break;
	
				default:
					kunde = em.find(Kunde.class, id);
					break;
			}
		}
		catch (NoResultException e) {
			return null;
		}

		return kunde;
	}
	
	public List<Kunde> findAllKunden(FetchType fetch) {
		final TypedQuery<Kunde> query = em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class);
		switch (fetch) {
			case NUR_KUNDE:
				break;
			case MIT_BESTELLUNGEN:
				query.setHint("javax.persistence.loadgraph", Kunde.GRAPH_BESTELLUNGEN);
				break;
			default:
				break;
		}
		
		final List<Kunde> kunden = query.getResultList();
		return kunden;
	}

	public Kunde findKundeByEmail(String email) {
		try {
			final Kunde kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL, Kunde.class)
					                      .setParameter(Kunde.PARAM_KUNDE_EMAIL, email)
					                      .getSingleResult();
			return kunde;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	public List<Kunde> findKundenByNachname(String nachname) {
		if (Strings.isNullOrEmpty(nachname)) {
			return null;
		}
		final List<Kunde> kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME, Kunde.class)
									 .setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
									 .getResultList();
		if(kunden == null) {
			throw new NotFoundException("Kein Kunde mit dem Namen \"" + nachname + "\" gefunden.");
		}
		return kunden;
	}
	
	public Kunde findKundeByUsername(String username) {
		try {
			return em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID, Kunde.class)
					 .setParameter(Kunde.PARAM_KUNDE_USERNAME, username)
					 .getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	public Kunde createKunde(Kunde kunde) {
		if (kunde == null) {
			return kunde;
		}
	
		// Pruefung, ob ein solcher Kunde schon existiert
		final Kunde tmp = findKundeByEmail(kunde.getEmail());
		if (tmp != null) {
			throw new EmailExistsException(kunde.getEmail());
		}
		
		// Password verschluesseln
		passwordVerschluesseln(kunde);
		
		// Rolle setzen
		kunde.addRollen(Sets.newHashSet(RolleType.KUNDE));
	
		em.persist(kunde);
		event.fire(kunde);
		
		return kunde;
	}

	public Kunde updateKunde(Kunde kunde) {
		if (kunde == null) {
			return null;
		}
		
		em.detach(kunde);
		
		final Kunde	tmp = findKundeByEmail(kunde.getEmail());
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getUsername() != kunde.getUsername()) {
				throw new EmailExistsException(kunde.getEmail());
			}
		}
		em.merge(kunde);
		return kunde;
	}

	public void deleteKunde(Kunde kunde) {
		if (kunde == null) {
			return;
		}
		
		try {
			kunde = findKundeById(kunde.getId(), FetchType.NUR_KUNDE);
		}
		catch (InvalidKundeIdException e) {
			return;
		}
		
		if (kunde == null) {
			return;
		}
		
		if (!kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}

		em.remove(kunde);
	}
	
	public Kunde findKundeByBestellungId(Long bestellungId) {
		try{
			final Kunde kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_BESTELLUNG_ID, Kunde.class)
				.setParameter(Kunde.PARAM_BESTELLUNG_ID, bestellungId)
				.getSingleResult();
		return kunde;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	private void passwordVerschluesseln(Kunde kunde) {
		LOGGER.debugf("passwordVerschluesseln BEGINN: %s", kunde);

		final String unverschluesselt = kunde.getPassword();
		final String verschluesselt = authService.verschluesseln(unverschluesselt);
		kunde.setPassword(verschluesselt);
		kunde.setPasswordWdh(verschluesselt);

		LOGGER.debugf("passwordVerschluesseln ENDE: %s", verschluesselt);
	}
	
	public Kunde setFile(Long kundeId, byte[] bytes) {
		final Kunde kunde = findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			return null;
		}
		final MimeType mimeType = fileHelper.getMimeType(bytes);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}
	
	public Kunde setFile(Kunde kunde, byte[] bytes, String mimeTypeStr) {
		final MimeType mimeType = MimeType.build(mimeTypeStr);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}
	
	private void setFile(Kunde kunde, byte[] bytes, MimeType mimeType) {
		if (mimeType == null) {
			throw new NoMimeTypeException();
		}
		
		final String filename = fileHelper.getFilename(kunde.getClass(), kunde.getId(), mimeType);
		
		// Gibt es noch kein (Multimedia-) File
		File file = kunde.getFile();
		if (kunde.getFile() == null) {
			file = new File(bytes, filename, mimeType);
			LOGGER.tracef("Neue Datei %s", file);
			kunde.setFile(file);
			em.persist(file);
		}
		else {
			file.set(bytes, filename, mimeType);
			LOGGER.tracef("Ueberschreiben der Datei %s", file);
			em.merge(file);
		}

		// Hochgeladenes Bild/Video/Audio in einem parallelen Thread als Datei fuer die Web-Anwendung abspeichern
		final File newFile = kunde.getFile();
		final Runnable storeFile = new Runnable() {
			@Override
			public void run() {
				fileHelper.store(newFile);
			}
		};
		managedExecutorService.execute(storeFile);
	}
}
