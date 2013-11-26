package de.shop.kundenverwaltung.rest;

import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.LIST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import com.google.common.base.Strings;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungResource;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.File;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.rest.UriHelper;

@Path("/kunden")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Log
public class KundeResource {
	

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
	public static final String KUNDE_USERNAME_QUERY_PARAM = "username";	
	public static final String KUNDEN_ID_PATH_PARAM = "id";
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpHeaders headers;
	
	@Inject
	private UriHelper uriHelper;
	
	@Inject
	private KundeService ks;
	
	@Inject 
	private BestellungService bs;
	
	@Inject
	private BestellungResource bestellungResource;
	
	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return "1.0";
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findKundeById(@PathParam("id") Long id) {
		final Kunde kunde = ks.findKundeById(id, FetchType.MIT_BESTELLUNGEN);
		if (kunde == null) {
			throw new NotFoundException("Kein Kunde mit der ID "+id+" gefunden.");
		}
		
		setStructuralLinks(kunde, uriInfo);
		
		return Response.ok(kunde)
				       .links(getTransitionalLinks(kunde, uriInfo))
				       .build();
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	public Response findBestellungenByKundeId(@PathParam("id") Long id) {
		final Kunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE);
		if (kunde == null) {
			throw new NotFoundException("Kein Kunde mit der ID " + id + " gefunden.");
		}
		final List<Bestellung> bestellungen = bs.findBestellungenByKundeId(kunde.getId());
		
		// URIs innerhalb der gefundenen Bestellungen anpassen
		if (bestellungen != null) {
			for (Bestellung bestellung : bestellungen) {
				bestellungResource.setStructuralLinks(bestellung, uriInfo);
			}
		}
		
		return Response.ok(new GenericEntity<List<Bestellung>>(bestellungen) {})
                       .links(getTransitionalLinksBestellungen(bestellungen, kunde, uriInfo))
                       .build();
	}
	
	//TODO findKundenByNachname überarbeiten - anscheinend werden Bestellungen nicht richtig mitgeladen
	@GET
	public Response findKundenByNachname(@QueryParam("nachname") @Pattern(regexp = Kunde.NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}") String nachname) {		
		List<Kunde> kunden = null;
		
		if ("".equals(nachname)) {
			kunden = ks.findAllKunden(FetchType.NUR_KUNDE);
		}
		else {
			kunden = ks.findKundenByNachname("%" + nachname + "%");
		}
		
		if(kunden.isEmpty()) {
			throw new NotFoundException("Keine Kunden mit dem Namen \"" + nachname + "\" gefunden.");
		}
		
		Object entity = null;
		Link[] links = null;
		
		if(kunden != null) {
			entity = new GenericEntity<List<Kunde>>(kunden) {};
			links = getTransitionalLinksKunden(kunden, uriInfo);
		}
		
		return Response.ok(entity)
					  .links(links)
					  .build();
	}
	
	private Link[] getTransitionalLinksBestellungen(List<Bestellung> bestellungen, 
													Kunde kunde,
													UriInfo uriInfo) {
		if (bestellungen == null || bestellungen.isEmpty()) {
			return new Link[0];
		}
		
		final Link self = Link.fromUri(getUriBestellungen(kunde, uriInfo))
                              .rel(SELF_LINK)
                              .build();
		
		final Link first = Link.fromUri(bestellungResource.getUriBestellung(bestellungen.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		
		final int lastPos = bestellungen.size() - 1;
		final Link last = Link.fromUri(bestellungResource.getUriBestellung(bestellungen.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { self, first, last };
	}

	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	@Transactional
	public Response createKunde(@Valid Kunde kunde) {
		kunde.setId(KEINE_ID);
		final Adresse adresse = kunde.getAdresse();
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		if (Strings.isNullOrEmpty(kunde.getPasswordWdh())) {
			// ein IT-System als REST-Client muss das Password ggf. nur 1x uebertragen
			kunde.setPasswordWdh(kunde.getPassword());
		}
		
		kunde = ks.createKunde(kunde);
		LOGGER.trace(kunde);
		
		return Response.created(getUriKunde(kunde, uriInfo))
				       .build();
	}
	
	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	@Transactional
	public Response updateKunde(@Valid Kunde kunde) {
		
		// Vorhandenen Kunden ermitteln
		final Kunde origKunde = ks.findKundeById(kunde.getId(),FetchType.NUR_KUNDE);
		if (origKunde == null) {
			throw new NotFoundException("Es gibt diesen Kunden nicht.");
		}
		LOGGER.tracef("Kunde vorher = %s", origKunde);
		
		// Daten des vorhandenen Kunden ueberschreiben
		origKunde.setValues(kunde);
		LOGGER.tracef("Kunde nachher = %s", origKunde);
		
		// Update durchfuehren
		kunde = ks.updateKunde(origKunde);
		setStructuralLinks(kunde, uriInfo);
		
		ks.updateKunde(kunde);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("{id:[1-9][0-9]*}")
	@Transactional
	public void deleteKunde(@PathParam("username") Long id) {
		final Kunde kunde = ks.findKundeById(id,FetchType.NUR_KUNDE);
		ks.deleteKunde(kunde);
	}
	
	public Link[] getTransitionalLinks(Kunde kunde, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriKunde(kunde, uriInfo))
	                          .rel(SELF_LINK)
	                          .build();

		final Link list = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
                              .rel(LIST_LINK)
                              .build();
		
		final Link add = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
                             .rel(ADD_LINK)
                             .build();

		final Link update = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
				                .rel(UPDATE_LINK)
				                .build();

		final Link remove = Link.fromUri(uriHelper.getUriKunde(KundeResource.class, "deleteKunde", kunde.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();

		return new Link[] { self, list, add, update, remove };
	}
	
	private Link[] getTransitionalLinksKunden(List<? extends Kunde> kunden, UriInfo uriInfo) {
		if (kunden == null || kunden.isEmpty()) {
			return null;
		}
		
		final Link first = Link.fromUri(getUriKunde(kunden.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		final int lastPos = kunden.size() - 1;
		final Link last = Link.fromUri(getUriKunde(kunden.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { first, last };
	}
	
	public void setStructuralLinks(Kunde kunde, UriInfo uriInfo) {
		// URI fuer Bestellungen setzen
		final URI uri = getUriBestellungen(kunde, uriInfo);
		kunde.setBestellungenUri(uri);
		
		LOGGER.trace(kunde);
	}
	
	public URI getUriKunde(Kunde kunde, UriInfo uriInfo) {
		return uriHelper.getUriKunde(KundeResource.class, "findKundeById", kunde.getId(), uriInfo);
	}
	
	private URI getUriBestellungen(Kunde kunde, UriInfo uriInfo) {
		return uriHelper.getUriKunde(KundeResource.class, "findBestellungenByKundeId", kunde.getId(), uriInfo);
	}
	
//	@Path("{id:[1-9][0-9]*}/file")
//	@POST
//	@Consumes({ "image/jpeg", "image/pjpeg", "image/png" })  // RESTEasy unterstuetzt nicht video/mp4
//	@Transactional
//	public Response upload(@PathParam("id") Long kundeId, byte[] bytes) {
//		ks.setFile(kundeId, bytes);
//		return Response.created(uriHelper.getUri(KundeResource.class, "download", kundeId, uriInfo))
//				       .build();
//	}
	
	@Path("{id:[1-9][0-9]*}/file")
	@GET
	@Produces({ "image/jpeg", "image/pjpeg", "image/png" })
	@Transactional  // Nachladen der Datei : AbstractKunde referenziert File mit Lazy Fetching
	public byte[] download(@PathParam("id") Long kundeId) {
		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			throw new NotFoundException("Der Kunde mit der ID "+kundeId+" wurde nicht gefunden.");
		}
		
		final File file = kunde.getFile();
		if (file == null) {
			throw new NotFoundException("Zu dem Kunden "+kundeId+" gibt es kein File.");
		}
		LOGGER.tracef("%s", file.toString());
		
		return file.getBytes();
	}
}
