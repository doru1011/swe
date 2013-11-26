package de.shop.bestellverwaltung.rest;

import static de.shop.util.TestConstants.LIEFERANT_ID_PATH_PARAM;
import static de.shop.util.TestConstants.LIEFERANT_ID_URI;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static de.shop.util.TestConstants.USERNAME;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.LIEFERANT_URI;
import static javax.ws.rs.client.Entity.json;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.filter;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.bestellverwaltung.domain.Lieferant;
import de.shop.util.AbstractResourceTest;


//Logging durch java.util.logging
/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */

@RunWith(Arquillian.class)
public class LieferantResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long LIEFERANT_ID_VORHANDEN = Long.valueOf(600);
	private static final Long LIEFERANT_ID_UPDATE = Long.valueOf(601);
	private static final Long LIEFERANT_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final String LIEFERANT_NAME_VORHANDEN = "UPS";
	private static final String LIEFERANT_NAME_NICHT_VORHANDEN = "xxx";	
	private static final String NEUER_NAME = "Nameneu";
	private static final String UPDATE_LIEFERANT_NAME = "Post";
	private static final BigDecimal NEUE_VERSANDKOSTEN = new BigDecimal("7.0");
	private static final Date NEU_ERSTELLT = new GregorianCalendar(2013, 0, 31).getTime();
	private static final Date NEU_AKTUALISIERT = new GregorianCalendar(2013, 0, 31).getTime();
	
	public String beginn = "BEGINN";
	public String ende = "ENDE";
	
	
	@Test
	@InSequence(1)
	public void findLieferantById() {
		LOGGER.finer(beginn);
		
		// Given
		final Long lieferantId = LIEFERANT_ID_VORHANDEN;
		
		// When
		final Response response = getHttpsClient().target(LIEFERANT_ID_URI)
                                                  .resolveTemplate(LIEFERANT_ID_PATH_PARAM, lieferantId)
                                                  .request()
                                                  .accept(APPLICATION_JSON)
                                                  .get();
		
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Lieferant lieferant = response.readEntity(Lieferant.class);
		
		assertThat(lieferant.getId()).isEqualTo(lieferantId);
		assertThat(lieferant.getName()).isNotEmpty();

		LOGGER.finer(ende);
	}
	
	@Test
	@InSequence(2)
	public void findLieferantByIdNichtVorhanden(){

		// Given
		final Long lieferantId = LIEFERANT_ID_NICHT_VORHANDEN;
		
		// When
		final Response response = getHttpsClient().target(LIEFERANT_ID_URI)
                                                  .resolveTemplate(LieferantResource.LIEFERANT_ID_PATH_PARAM, lieferantId)
                                                  .request()
                                                  .acceptLanguage(GERMAN)
                                                  .get();

    	// Then
    	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Kein Lieferant mit der ID")
    	                         .endsWith("gefunden.");
		
		LOGGER.finer(ende);
	}
	

	@Test
	@InSequence(10)
	public void findLieferantbyName(){
		LOGGER.fine(beginn);
		
		//Given
		final String name = LIEFERANT_NAME_VORHANDEN;
		
		//When
		Response response = getHttpsClient().target(LIEFERANT_URI)
										   .queryParam(LieferantResource.LIEFERANT_NAME_QUERY_PARAM, name)
										   .request()
										   .accept(APPLICATION_JSON)
										   .get();
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);			
		LOGGER.fine(ende);
		
	}
	
	@Test
	@InSequence(11)
	public void findLieferantByNameNichtVorhanden(){
		LOGGER.fine(beginn);
		
		//Given
		final String name = LIEFERANT_NAME_NICHT_VORHANDEN;
		
		//When
		Response response = getHttpsClient().target(LIEFERANT_URI)
										   .queryParam(LieferantResource.LIEFERANT_NAME_QUERY_PARAM, name)
										   .request()
										   .accept(APPLICATION_JSON)
										   .get();
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).isEqualTo("Kein Lieferant mit dem Namen \"" + name + "\" gefunden.");		
		
		LOGGER.fine(ende);		
	}
	
	
	@Test
	@InSequence(30)
	public void createLieferant() {
		LOGGER.finer(beginn);
		
		// Given
		final String name = NEUER_NAME;		
		final BigDecimal versandkosten = NEUE_VERSANDKOSTEN;
		final Date erstellt = NEU_ERSTELLT;
		final Date aktualisiert = NEU_AKTUALISIERT;
		
		
		final Lieferant lieferant = new Lieferant();
		lieferant.setName(name);		
		lieferant.setVersandkosten(versandkosten);
		lieferant.setErstellt(erstellt);
		lieferant.setAktualisiert(aktualisiert);
		
		Response response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_URI)
                                                              .request()
                                                              .post(json(lieferant));
			
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id).isPositive();				
		
		// Gibt es den neuen Lieferant?
		response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_ID_URI)
                				   .resolveTemplate(LIEFERANT_ID_PATH_PARAM, id)
                				   .request()
                				   .accept(APPLICATION_JSON)
                				   .get();
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();

		LOGGER.finer(ende);
	}
	
	@Test
	@InSequence(31)
	public void createInvalidLieferant(){
		LOGGER.finer(beginn);
		
		//Given
		final String name = "A";
		final int version = 0;			
		final BigDecimal versandkosten = null;
		
		final Lieferant lieferant = new Lieferant();
		lieferant.setName(name);
		lieferant.setVersion(version);
		lieferant.setErstellt(NEU_ERSTELLT);
		lieferant.setVersandkosten(versandkosten);		
		lieferant.setAktualisiert(NEU_AKTUALISIERT);
		
		Response response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_URI)
											.request()
											.accept(APPLICATION_JSON)
											.acceptLanguage(ENGLISH)
											.post(json(lieferant));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
		assertThat(response.getHeaderString("validation-exception")).isEqualTo("true");
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		response.close();
		
		final List<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(violations).isNotEmpty();		
		
		// TODO {min} richtig in properties einlesen
		ResteasyConstraintViolation violation =
                filter(violations).with("message")
                                  .equalsTo("The delivery name must have at least 2 chars.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(name));		

		violation =
                filter(violations).with("message")
                                  .equalsTo("The delivery-costs must not be empty.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEmpty();
		
				LOGGER.finer(ende);	
	}
	
	@Test
	@InSequence(40)
	public void updateLieferant(){
		LOGGER.finer(beginn);
		//TODO Username und Passwort in getHttpsClients einfügen
		//Given
		final Long lieferantId = LIEFERANT_ID_UPDATE;
		final String neuerName = UPDATE_LIEFERANT_NAME;
		
		//WHEN 
		Response response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_ID_URI)
											.resolveTemplate(LieferantResource.LIEFERANT_ID_PATH_PARAM, lieferantId)
											.request()
											.accept(APPLICATION_JSON)
											.get();
		
		Lieferant lieferant = response.readEntity(Lieferant.class);
		assertThat(lieferant.getId()).isEqualTo(lieferantId);
		final int origVersion = lieferant.getVersion();
		
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Namen bauen
		lieferant.setName(neuerName);
		
		response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_URI)
								   .request()
								   .accept(APPLICATION_JSON)
								   .put(json(lieferant));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		lieferant = response.readEntity(Lieferant.class);
		assertThat(lieferant.getVersion()).isGreaterThan(origVersion);
		
		// Erneutes Update funktioniert, da die Versionsnr. aktualisiert ist
		lieferant.setName("test");
		response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_URI)
		                           .request()
		                           .put(json(lieferant));
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();
				
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		lieferant.setName("testerich");
		response = getHttpsClient(USERNAME, PASSWORD).target(LIEFERANT_URI)
		                           .request()
		                           .put(json(lieferant));
		assertThat(response.getStatus()).isEqualTo(HTTP_CONFLICT);
		response.close();
				
		LOGGER.finer(ende);
	}	
}
