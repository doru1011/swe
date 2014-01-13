package de.shop.artikelverwaltung.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

import com.google.common.base.Strings;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.persistence.ConcurrentDeletedException;

@RolesAllowed({"mitarbeiter", "admin" })
@SecurityDomain("shop")
@Log
public class ArtikelService implements Serializable {
	private static final long serialVersionUID = -5105686816948437276L;
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
	
	public Artikel findArtikelById(Long id) {
		
		final Artikel artikel = em.find(Artikel.class, id);
		if (artikel == null) {
			throw new NotFoundException("Kein Artikel mit der ID \"" + id + "\" gefunden.");
		}
		return artikel;
	}
	
	public List<Artikel> findAllArtikel() {
		final List<Artikel> artikel = em.createNamedQuery(Artikel.FIND_ALL_ARTIKEL, Artikel.class).getResultList();
		return artikel;
		}
		
		public List<Artikel> findArtikelByName(String name) {
			if (Strings.isNullOrEmpty(name)) {
				return null;
			}
			final List<Artikel> artikel = em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_NAME, Artikel.class)
													.setParameter(Artikel.PARAM_NAME, name)
													.getResultList();
			if (artikel == null) {
				throw new NotFoundException("Kein Artikel mit  dem Namen \"" + name + "\" gefunden.");
			}
			return artikel;
		}
		
	public List<Artikel> findArtikelByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
	
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Artikel> criteriaQuery = builder.createQuery(Artikel.class);
		final Root<Artikel> a = criteriaQuery.from(Artikel.class);
		final Path<Long> idPath = a.get("id");
		//final Path<String> idPath = a.get(Artikel_.id);   // Metamodel-Klassen funktionieren nicht mit Eclipse
				
		Predicate pred = null;
		if (ids.size() == 1) {
			// Genau 1 id: kein OR notwendig
			pred = builder.equal(idPath, ids.get(0));
		}
		else {
			// Mind. 2x id, durch OR verknuepft
			final Predicate[] equals = new Predicate[ids.size()];
			int i = 0;
			for (Long id : ids) {
				equals[i++] = builder.equal(idPath, id);
			}
				
			pred = builder.or(equals);
		}
		criteriaQuery.where(pred);
			
		return em.createQuery(criteriaQuery)
				 .getResultList();
	}

	
		public Artikel createArtikel(Artikel artikel) {
			if (artikel == null) {
				return null;
			}
			

			try {
				em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_NAME, Artikel.class)
				.setParameter(Artikel.PARAM_NAME, artikel.getName())
				.getSingleResult();
				throw new ArtikelNameExistsException(artikel.getName());
			}
			catch (NoResultException e) {
				//Noch kein Artikel mit diesem Namen
				LOGGER.trace("Name existiert noch nicht.");
			}
		
			em.persist(artikel);
			return artikel;
		}
		

		public Artikel updateArtikel(Artikel artikel) throws ArtikelNameExistsException {
			if (artikel == null) {
				return null;
			}
			em.detach(artikel);
			
			// Wurde das Objekt konkurrierend geloescht?
			final Artikel tmp = findArtikelById(artikel.getId());
			if (tmp == null) {
				throw new ConcurrentDeletedException(artikel.getId());
			}
			em.detach(tmp);
			
//			//Gibt es einen Artikel mit gleichem Namen?
			final List<Artikel> tmpList = findArtikelByName(artikel.getName());
			for (Artikel a : tmpList) {
				if (a.getId().longValue() != artikel.getId().longValue() && a.getName() == artikel.getName()) {
					throw new ArtikelNameExistsException("Ein Artikel mit dem Namen \"" 
				+ artikel.getName() + "\" existiert bereits.");
				}
			}
						
			artikel = em.merge(artikel);
			return artikel;
		}
		/**
		 * Liste der wenig bestellten Artikel ermitteln
		 * @param anzahl Obergrenze fuer die maximale Anzahl der Bestellungen
		 * @return Liste der gefundenen Artikel
		 */
		public List<Artikel> ladenhueter(int anzahl) {
			return em.createNamedQuery(Artikel.FIND_LADENHUETER, Artikel.class)
					 .setMaxResults(anzahl)
					 .getResultList();
		}

	}
