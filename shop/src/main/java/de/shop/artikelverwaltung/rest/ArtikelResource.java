package de.shop.artikelverwaltung.rest;

import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.rest.UriHelper;

@Path("/artikel")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes
@RequestScoped
@Log
public class ArtikelResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	// public fuer Testklassen
	public static final String ARTIKEL_ID_PATH_PARAM = "artikelId";
	public static final String ARTIKEL_NAME_QUERY_PARAM = "name";
	
	@Context
	private UriInfo uriInfo;
		
	@Inject
	private UriHelper uriHelper;
	
	@Inject
	private ArtikelService as;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return "1.0";
	}
	
	@POST
	@Consumes(APPLICATION_JSON)
	@Transactional
	public Response createArtikel(@Valid Artikel artikel) {
		artikel = as.createArtikel(artikel);
		
		LOGGER.trace(artikel);
		return Response.created(getUriArtikel(artikel,uriInfo)).build();
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findArtikelById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
		final Artikel artikel = as.findArtikelById(id);
		if (artikel == null) {
			throw new NotFoundException("Artikel nicht gefunden" + id);
		}

		return Response.ok(artikel)
	                   .links(getTransitionalLinks(artikel, uriInfo))
	                   .build();
	}
		
	private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriArtikel(artikel, uriInfo))
                              .rel(SELF_LINK)
                              .build();

		return new Link[] { self };
	}
	
	private Link[] getTransitionalLinksArtikel(List<? extends Artikel> artikel, UriInfo uriInfo) {
		if (artikel == null || artikel.isEmpty()) {
			return null;
		}
		
		final Link first = Link.fromUri(getUriArtikel(artikel.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		final int lastPos = artikel.size() - 1;
		final Link last = Link.fromUri(getUriArtikel(artikel.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { first, last };
	}
	
	public URI getUriArtikel(Artikel artikel, UriInfo uriInfo) {
		return uriHelper.getUri(ArtikelResource.class, "findArtikelById", artikel.getId(), uriInfo);
	}
		
	@GET
	public Response findArtikelByName(@QueryParam("name") @DefaultValue("") String name) {
		
		List<Artikel> artikel = null;
		
		if ("".equals(name)) {
			artikel = as.findAllArtikel();
		}
		else {
			artikel = as.findArtikelByName("%" + name + "%");
		}

		if (artikel.isEmpty()) {
			throw new NotFoundException("Keine Artikel mit dem Namen \"" + name + "\" gefunden.");
		}
		
		Object entity = null;
		Link[] links = null;
		
		if(artikel != null){
			entity = new GenericEntity<List<Artikel>>(artikel){};
			links = getTransitionalLinksArtikel(artikel, uriInfo);
		}
		
		return Response.ok(entity)
					   .links(links)
					   .build();
	}
	
	
	
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Transactional
	public Response updateArtikel(@Valid Artikel artikel) {
		
		//Vorhandenen Artikel finden
		final Artikel origArtikel = as.findArtikelById(artikel.getId());
		if(origArtikel == null) {
			throw new NotFoundException("Kein Artikel mit ID " + artikel.getId() + " gefunden");
		}
		
		LOGGER.tracef("Artikel vorher = %s", origArtikel);
		
		// Daten des vorhandenen Kunden ueberschreiben
		origArtikel.setValues(artikel);
		LOGGER.tracef("Kunde nachher = %s", origArtikel);
			
		//updaten
		artikel = as.updateArtikel(origArtikel);

		return Response.ok(artikel)
					   .links(getTransitionalLinks(artikel,uriInfo))
					   .build();					   
	}
}
