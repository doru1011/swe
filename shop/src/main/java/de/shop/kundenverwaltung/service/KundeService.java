package de.shop.kundenverwaltung.service;

import static de.shop.util.Constants.MAX_AUTOCOMPLETE;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Date;

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
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.Log;
import de.shop.util.persistence.MimeType;
import de.shop.util.NoMimeTypeException;
import de.shop.util.NotFoundException;
import de.shop.util.persistence.ConcurrentDeletedException;

@RolesAllowed({"mitarbeiter", "admin" })
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
	
	/**
	 * Potenzielle IDs zu einem gegebenen ID-Praefix suchen
	 * @param idPrefix der Praefix zu potenziellen IDs als String
	 * @return Liste der passenden Praefixe
	 */
	
	public List<Long> findIdsByPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		final List<Long> ids = em.createNamedQuery(Kunde.FIND_IDS_BY_PREFIX, Long.class)
				                 .setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
				                 .getResultList();
		return ids;
	}
	/**
	 * Kunden suchen, deren ID den gleiche Praefix hat.
	 * @param id Praefix der ID
	 * @return Liste mit Kunden mit passender ID
	 */
	public List<Kunde> findKundenByIdPrefix(Long id) {
		if (id == null) {
			return Collections.emptyList();
		}
		
		return em.createNamedQuery(Kunde.FIND_KUNDEN_BY_ID_PREFIX, Kunde.class)
				 .setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
				 .setMaxResults(MAX_AUTOCOMPLETE)
				 .getResultList();
	}
	
	/**
	 * Alle Kunden in einer bestimmten Reihenfolge ermitteln
	 * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen.
	 * @param order Sortierreihenfolge, z.B. nach aufsteigenden IDs.
	 * @return Liste der Kunden
	 */
	public List<Kunde> findAllKunden(FetchType fetch, OrderByType order) {
		final TypedQuery<Kunde> query = OrderByType.USERNAME.equals(order)
				                        ? em.createNamedQuery(Kunde.FIND_KUNDEN_ORDER_BY_USERNAME,
										                      Kunde.class)
				                        : em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class);
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
	
//	public List<Kunde> findAllKunden(FetchType fetch) {
//		final TypedQuery<Kunde> query = em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class);
//		switch (fetch) {
//			case NUR_KUNDE:
//				break;
//			case MIT_BESTELLUNGEN:
//				query.setHint("javax.persistence.loadgraph", Kunde.GRAPH_BESTELLUNGEN);
//				break;
//			default:
//				break;
//		}
//		
//		final List<Kunde> kunden = query.getResultList();
//		return kunden;
//	}

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
	
	/**
	 * Kunden mit gleichem Nachnamen suchen.
	 * @param nachname Der gemeinsame Nachname der gesuchten Kunden
	 * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen
	 * @return Liste der gefundenen Kunden
	 */
	public List<Kunde> findKundenByNachname(String nachname, FetchType fetch) {
		List<Kunde> kunden;
		switch (fetch) {
			case NUR_KUNDE:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME, Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;
			
			case MIT_BESTELLUNGEN:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
						                     Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;

			default:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME, Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;
		}
		
		// FIXME https://hibernate.atlassian.net/browse/HHH-8285 : @NamedEntityGraph ab Java EE 7 bzw. JPA 2.1
		//final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
		//                                                            AbstractKunde.class)
		//				                          .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname);
		//switch (fetch) {
		//	case NUR_KUNDE:
		//		break;
		//	case MIT_BESTELLUNGEN:
		//		query.setHint("javax.persistence.loadgraph", AbstractKunde.GRAPH_BESTELLUNGEN);
		//		break;
		//	case MIT_WARTUNGSVERTRAEGEN:
		//		query.setHint("javax.persistence.loadgraph", AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
		//		break;
		//	default:
		//		break;
		//}
		//
		//final List<AbstractKunde> kunden = query.getResultList();
		return kunden;
	}
	
	
	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		return em.createNamedQuery(Kunde.FIND_NACHNAMEN_BY_PREFIX, String.class)
				 .setParameter(Kunde.PARAM_KUNDE_NACHNAME_PREFIX, nachnamePrefix + '%')
				 .getResultList();
	}	
	
		
	public List<Kunde> findKundenByNachname(String nachname) {
		if (Strings.isNullOrEmpty(nachname)) {
			return null;
		}
		final List<Kunde> kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME, Kunde.class)
									 .setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
									 .getResultList();
		if (kunden == null) {
			throw new NotFoundException("Kein Kunde mit dem Namen \"" + nachname + "\" gefunden.");
		}
		return kunden;
	}
	//TODO NOCH ANPASSEN
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
	
	/**
	 * Einen vorhandenen Kunden aktualisieren
	 * @param kunde Der aktualisierte Kunde
	 * @param geaendertPassword Wurde das Passwort aktualisiert und muss es deshalb verschluesselt werden?
	 * @return Der aktualisierte Kunde
	 */
	public Kunde updateKunde(Kunde kunde, boolean geaendertPassword) {
		if (kunde == null) {
			return null;
		}
		
		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id und Email gesucht wird
		em.detach(kunde);
		
		// Wurde das Objekt konkurrierend geloescht?
		Kunde tmp = findKundeById(kunde.getId(), FetchType.NUR_KUNDE);
		if (tmp == null) {
			throw new ConcurrentDeletedException(kunde.getId());
		}
		em.detach(tmp);
		
		// Gibt es ein anderes Objekt mit gleicher Email-Adresse?
		tmp = findKundeByEmail(kunde.getEmail());
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getId().longValue() != kunde.getId().longValue()) {
				// anderes Objekt mit gleichem Attributwert fuer email
				throw new EmailExistsException(kunde.getEmail());
			}
		}
		
		// Password verschluesseln
		if (geaendertPassword) {
			passwordVerschluesseln(kunde);
		}

		kunde = em.merge(kunde);   // OptimisticLockException
		kunde.setPasswordWdh(kunde.getPassword());
		
		return kunde;
	}

//	public Kunde updateKunde(Kunde kunde) {
//		if (kunde == null) {
//			return null;
//		}
//		
//		em.detach(kunde);
//		
//		final Kunde	tmp = findKundeByEmail(kunde.getEmail());
//		if (tmp != null) {
//			em.detach(tmp);
//			if (tmp.getUsername() != kunde.getUsername()) {
//				throw new EmailExistsException(kunde.getEmail());
//			}
//		}
//		em.merge(kunde);
//		return kunde;
//	}

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
	
	/**
	 * Kunden suchen, die seit einem bestimmten Datum Kunde sind.
	 * @param seit Das Datum
	 * @return Liste der gefundenen Kunden
	 */
	public List<Kunde> findKundenBySeit(Date seit) {
		return em.createNamedQuery(Kunde.FIND_KUNDEN_BY_DATE, Kunde.class)
				 .setParameter(Kunde.PARAM_KUNDE_SEIT, seit)
				 .getResultList();
	}
	
	public Kunde findKundeByBestellungId(Long bestellungId) {
		try {
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
