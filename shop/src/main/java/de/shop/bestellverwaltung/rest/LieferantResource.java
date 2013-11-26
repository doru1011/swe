package de.shop.bestellverwaltung.rest;

import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
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
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.GenericEntity;

import de.shop.bestellverwaltung.domain.Lieferant;
import de.shop.bestellverwaltung.service.LieferantService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.rest.UriHelper;

import org.jboss.logging.Logger;

@Path("/lieferant")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes
@RequestScoped
@Log
public class LieferantResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	// public fuer Testklassen
	public static final String LIEFERANT_ID_PATH_PARAM = "lieferantId";
	public static final String LIEFERANT_NAME_QUERY_PARAM = "name";
	
	@Context
	private UriInfo uriInfo;
	
	@Inject
	private UriHelper uriHelper;
	
	@Inject
	private LieferantService ls;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findLieferantById(@PathParam("id") Long lieferantId, @Context UriInfo uriInfo) {
		
		final Lieferant lieferant = ls.findLieferantById(lieferantId);		
		if (lieferant == null) {
			throw new NotFoundException("Kein Lieferant mit der ID \"" + lieferantId + " gefunden.");
		}
		
		return Response.ok(lieferant)
                	   .links(getTransitionalLinks(lieferant, uriInfo))
					   .build();
	}
	
	@GET
	public Response findLieferantByName(@QueryParam("name") @DefaultValue("") String lieferantName) {
		
		List<? extends Lieferant> lieferanten = null;
		
		if("".equals(lieferantName)){
			lieferanten = ls.findAllLieferanten();
		}
		else {
			lieferanten = ls.findLieferantByName("%" + lieferantName + "%");
		}
		
		if(lieferanten.isEmpty()){
			throw new NotFoundException("Kein Lieferant mit dem Namen \"" + lieferantName + "\" gefunden.");
		}
		
		Object entity = null;
		Link[] links = null;
		
		if(lieferanten != null){
			entity = new GenericEntity<List<? extends Lieferant>>(lieferanten) { };
			links = getTransitionalLinksLieferant(lieferanten, uriInfo);
		}
		return Response.ok(entity)
					   .links(links)
				       .build();
}
	
	@POST
	@Consumes(APPLICATION_JSON)
	@Transactional
	public Response createLieferant(@Valid Lieferant lieferant) {
		lieferant = ls.createLieferant(lieferant);
		
		LOGGER.trace(lieferant);
		return Response.created(getUriLieferant(lieferant, uriInfo)).build();
	}
	
	public URI getUriLieferant(Lieferant lieferant, UriInfo uriInfo) {
		return uriHelper.getUri(LieferantResource.class, "findLieferantById", lieferant.getId(), uriInfo);
	}
	
	private Link[] getTransitionalLinks(Lieferant lieferant, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriLieferant(lieferant, uriInfo))
							  .rel(SELF_LINK)
							  .build();
		return new Link[] {self};
	}
	
	private Link[] getTransitionalLinksLieferant(List<? extends Lieferant> lieferant, UriInfo uriInfo) {
		if (lieferant == null || lieferant.isEmpty()) {
			return null;
		}
		
		final Link first = Link.fromUri(getUriLieferant(lieferant.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		final int lastPos = lieferant.size() - 1;
		final Link last = Link.fromUri(getUriLieferant(lieferant.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { first, last };
	}
	
	

	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Transactional
	public Response updateLieferant(@Valid Lieferant lieferant) {
		
		//Vorhandenen Artikel finden
		final Lieferant origLieferant = ls.findLieferantById(lieferant.getId());
		if(origLieferant == null){
			throw new NotFoundException("Kein Artikel mit ID " + lieferant.getId() + " gefunden");
		}
		
		LOGGER.tracef("Lieferant vorher = %s", origLieferant);
		
		// Daten des vorhandenen Kunden ueberschreiben
		origLieferant.setValues(lieferant);
		LOGGER.tracef("Lieferant nachher = %s", origLieferant);
			
		//updaten
		lieferant = ls.updateLieferant(origLieferant);
		return Response.ok(lieferant)
					   .links(getTransitionalLinks(lieferant, uriInfo))
					   .build();					   
	}
}
