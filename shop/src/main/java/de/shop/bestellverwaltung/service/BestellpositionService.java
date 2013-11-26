//package de.shop.bestellverwaltung.service;
//
//import java.io.Serializable;
//import java.lang.invoke.MethodHandles;
//import java.util.Collection;
//import java.util.Locale;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.annotation.security.RolesAllowed;
//import javax.ejb.Stateless;
//import javax.inject.Inject;
//import javax.persistence.EntityManager;
//import javax.persistence.NoResultException;
//
//import org.jboss.ejb3.annotation.SecurityDomain;
//import org.jboss.logging.Logger;
//
//import de.shop.bestellverwaltung.domain.Bestellposition;
//import de.shop.util.Log;
//import de.shop.util.NotFoundException;
//
//@Stateless
//@RolesAllowed({"mitarbeiter", "gruppenleiter"})
//@SecurityDomain("shop")
//@Log
//public class BestellpositionService implements Serializable {
//	private static final long serialVersionUID = 3188789767052580247L;
//	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
//	
//	@Inject
//	private transient EntityManager em;
//	
//	@PostConstruct
//	private void postConstruct() {
//		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
//	}
//	
//	@PreDestroy
//	private void preDestroy() {
//		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
//	}
//
//	public Bestellposition createBestellposition(Bestellposition bestellposition, Locale locale) {
//		if (bestellposition == null) {
//			return null;
//		}
//	
//		try {
//			final Collection<Bestellposition> bestellpositionen = em
//					.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNGID, Bestellposition.class)
//			.setParameter(Bestellposition.PARAM_BESTELLUNG, bestellposition.getBestellung().getId()).getResultList();
//			
//			for (Bestellposition bps : bestellpositionen) {
//				if (bps.getId() != null)
//					throw new BestellpositionIdExistsException(bestellposition.getId());
//			}
//		}
//		catch (NoResultException e) {
//			LOGGER.trace("Bestellposition mit dieser ID existiert noch nicht.");
//		}
//		return bestellposition;
//	}
//
//	public Bestellposition updateBestellposition(Bestellposition bestellposition) {
//		if (bestellposition == null) {
//			return null;
//		}
//		final Bestellposition vorhandeneBestellposition = em
//									.createNamedQuery(Bestellposition.FIND_BESTELLPOSITION_BY_ID, Bestellposition.class)
//									.setParameter(Bestellposition.PARAM_ID, bestellposition.getId())
//									.getSingleResult();
//		
//		if (vorhandeneBestellposition == null)
//			throw new NotFoundException("Die Bestellposition konnte nicht aktualisiert" 
//										+ "werden da keine Bestellposition mit der ID '"
//										+ bestellposition.getId() + "' existiert.");
//		if (vorhandeneBestellposition.getId().longValue() != bestellposition.getId().longValue()) {
//			throw new BestellpositionIdExistsException(bestellposition.getId());
//		}
//		em.merge(bestellposition);
//		return bestellposition;
//	}
//
//	public Bestellposition findBestellpositionById(Long id) {	
//		final Bestellposition bestellposition = em
//				.createNamedQuery(Bestellposition.FIND_BESTELLPOSITION_BY_ID, Bestellposition.class)
//				.setParameter(Bestellposition.PARAM_ID, id)
//				.getSingleResult();
//		
//		return bestellposition;
//	}
//	
//	public Collection<Bestellposition> findBestellpositionenByBestellungId(Long bestellungId) {	
//		final Collection<Bestellposition> bestellpositionen = em
//				.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNGID, Bestellposition.class)
//				.setParameter(Bestellposition.PARAM_BESTELLUNG, bestellungId)
//				.getResultList();
//		
//		if (bestellpositionen == null)
//			throw new NotFoundException("Die Bestellung enthält keine Bestellpositionen");
//		
//		return bestellpositionen;
//	}	
//	
//	public void deleteBestellposition(Long bestellpositionId) {
//		final Bestellposition bestellposition = findBestellpositionById(bestellpositionId);
//		if (bestellposition == null) {
//			return;
//		}		
//		
//		em.remove(bestellposition);
//	}
//}
