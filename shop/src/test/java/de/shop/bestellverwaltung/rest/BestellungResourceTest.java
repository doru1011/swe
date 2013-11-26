package de.shop.bestellverwaltung.rest;

import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.ARTIKEL_ID_URI;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_KUNDE_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.USERNAME;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.artikelverwaltung.rest.ArtikelResource;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.NeueBestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.rest.KundeResource;
import de.shop.util.AbstractResourceTest;


//Logging durch java.util.logging
/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */

@RunWith(Arquillian.class)
public class BestellungResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long BESTELLUNG_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(401);
	private static final Long ARTIKEL_ID_VORHANDEN_1 = Long.valueOf(300);
	private static final Long ARTIKEL_ID_VORHANDEN_2 = Long.valueOf(301);
	
	

	
	@Test
	@InSequence(1)
	public void findBestellungById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
		
		// When
		final Response response = getHttpsClient(USERNAME, PASSWORD).target(BESTELLUNGEN_ID_URI)
                                                  .resolveTemplate(BestellungResource.BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                                  .request()
                                                  .accept(APPLICATION_JSON)
                                                  .get();
		
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Bestellung bestellung = response.readEntity(Bestellung.class);
		
		assertThat(bestellung.getId()).isEqualTo(bestellungId);
		assertThat(bestellung.getBestellpositionen()).isNotEmpty();

		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(2)
	public void findBestellungByIdNichtVorhanden(){

		// Given
		final Long bestellungId = BESTELLUNG_ID_NICHT_VORHANDEN;
		
		// When
		final Response response = getHttpsClient(USERNAME, PASSWORD).target(BESTELLUNGEN_ID_URI)
                                                  .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                                  .request()
                                                  .acceptLanguage(GERMAN)
                                                  .get();

    	// Then
    	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Bestellung nicht gefunden" + bestellungId);
    	                         
		
		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(3)
	public void findKundeByBestellungId() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
		
		// When
		Response response = getHttpsClient(USERNAME, PASSWORD).target(BESTELLUNGEN_ID_KUNDE_URI)
                                            .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                            .request()
                                            .accept(APPLICATION_JSON)
                                            .get();
			
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Kunde kunde = response.readEntity(Kunde.class);
		assertThat(kunde).isNotNull();
		
		response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_ID_URI)
                                   .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kunde.getId())
                                   .request()
                                   .accept(APPLICATION_JSON)
                                   .get();
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		assertThat(response.getLinks()).isNotEmpty();
		response.close();    // response.readEntity() wurde nicht aufgerufen

		LOGGER.finer("ENDE");
	}
	
	
	
	@Test
	@InSequence(4)
	public void findKundeByBestellungIdNichtVorhanden() {
		
LOGGER.finer("BEGINN");
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_NICHT_VORHANDEN;
		
		// When
		Response response = getHttpsClient(USERNAME, PASSWORD).target(BESTELLUNGEN_ID_KUNDE_URI)
                                            .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                            .request()
                                            .accept(APPLICATION_JSON)
                                            .get();
			
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
		
		final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Keinen Kunden zu der BestellungsId: " + bestellungId);
    	
    	
		LOGGER.finer("ENDE");
		
	}
	
	@Test
	@InSequence(5)
	public void createBestellung() throws URISyntaxException {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long artikelId1 = ARTIKEL_ID_VORHANDEN_1;
		final Long artikelId2 = ARTIKEL_ID_VORHANDEN_2;
		
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final Bestellung bestellung = new Bestellung();
		
		Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId1));
		bp.setMenge((long) 1);
		bestellung.addBestellposition(bp);

		bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId2));
		bp.setMenge((long) 1);
		bestellung.addBestellposition(bp);
		
		// When
		Long id;
		Response response = getHttpsClient(USERNAME,PASSWORD).target(BESTELLUNGEN_URI)
                                                              .request()
                                                              .post(json(bestellung));
			
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		final String location = response.getLocation().toString();
		response.close();
			
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		id = Long.valueOf(idStr);
		assertThat(id).isPositive();
		
		// Gibt es die neue Bestellung?
		response = getHttpsClient(USERNAME,PASSWORD).target(BESTELLUNGEN_ID_URI)
                                   .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, id)
                                   .request()
                                   .accept(APPLICATION_JSON)
                                   .get();
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();
		
		LOGGER.finer("ENDE");
	}	
}
