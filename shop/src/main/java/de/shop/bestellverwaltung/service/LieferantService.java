package de.shop.bestellverwaltung.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

import com.google.common.base.Strings;

import de.shop.artikelverwaltung.service.ArtikelNameExistsException;
import de.shop.bestellverwaltung.domain.Lieferant;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.persistence.ConcurrentDeletedException;

@RolesAllowed({"mitarbeiter", "admin"})
@SecurityDomain("shop")
@Log
public class LieferantService implements Serializable {
	private static final long serialVersionUID = 3188789767052580247L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private transient EntityManager em;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	public Lieferant findLieferantById(Long lieferantId) {	
		
		final Lieferant lieferant = em.find(Lieferant.class, lieferantId);		
		if(lieferant == null) {
			throw new NotFoundException("Kein Lieferant mit der ID\""+ lieferantId + "\" gefunden.");
		}
		return lieferant;
	}
	
	public List<Lieferant> findAllLieferanten() {
		final List<Lieferant> result = em.createNamedQuery(Lieferant.FIND_ALL_LIEFERANTEN, Lieferant.class)
										.getResultList();
		return result;
	}
	
	public List<Lieferant> findLieferantByName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			return null;
		}
			final List<Lieferant> lieferant = em.createNamedQuery(Lieferant.FIND_LIEFERANT_BY_NAME, Lieferant.class)
											.setParameter(Lieferant.PARAM_LIEFERANT_BEZEICHNUNG, name)
											.getResultList();
			if(lieferant == null) {
				throw new NotFoundException("Kein Lieferant mit dem Namen \""  + name+ "\" gefunden.");
		}
		return lieferant;
	}		
	
	public Lieferant createLieferant(Lieferant lieferant) {
		if (lieferant == null) {
			return null;
		}		
		
		try {
			em.createNamedQuery(Lieferant.FIND_LIEFERANT_BY_NAME, Lieferant.class)
			.setParameter(Lieferant.PARAM_LIEFERANT_BEZEICHNUNG, lieferant.getName())
			.getSingleResult();
			throw new LieferantNameExistsException(lieferant.getName());
		}
		//TODO LieferantNameExistsException in NoResultException Hierarchie
		catch (NoResultException e) {
			//Noch kein Lieferant mit diesem Namen
			LOGGER.trace("Lieferantenname existiert noch nicht.");
		}
				
		em.persist(lieferant);
		return lieferant;
	}

	public Lieferant updateLieferant(Lieferant lieferant) throws LieferantNameExistsException {
		if (lieferant == null) {
			return null;
		}
		em.detach(lieferant);
		
		// Wurde das Objekt konkurrierend geloescht?
		Lieferant tmp = findLieferantById(lieferant.getId());
		if(tmp == null){
			throw new ConcurrentDeletedException(lieferant.getId());
		}
		em.detach(tmp);
		
		//Gibt es einen Artikel mit gleichem Namen?
		List<Lieferant> tmpList = findLieferantByName(lieferant.getName());
		for(Lieferant a : tmpList){
			if(a.getId().longValue() != lieferant.getId().longValue() && a.getName() == lieferant.getName()){
				throw new ArtikelNameExistsException("Ein Lieferant mit dem Namen \"" + lieferant.getName() +"\" existiert bereits.");
			}
			em.detach(a);
		}
					
		lieferant = em.merge(lieferant);
		return lieferant;
	}
	
//	public void deleteLieferant(Lieferant lieferant) {
//		if (!em.contains(lieferant)) {
//			lieferant = em.find(Lieferant.class, lieferant.getId());
//			if (lieferant == null)
//			return;
//			}	
//				
//		em.remove(lieferant);
//	}
}
