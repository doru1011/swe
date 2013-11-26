package de.shop.artikelverwaltung.rest;


import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.TestConstants.ARTIKEL_ID_URI;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.USERNAME;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.filter;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.domain.KategorieType;
import de.shop.util.AbstractResourceTest;

@RunWith(Arquillian.class)
public class ArtikelResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(301);
	private static final Long ARTIKEL_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long ARTIKEL_ID_UPDATE = Long.valueOf(302);
	
	private static final String INVALID_ARTIKEL_NAME = "{[]}/";
	private static final String INVALID_ARTIKEL_BESCHREIBUNG = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
															 + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
	
	private static final String ARTIKEL_NAME_VORHANDEN = "Schrank";
	private static final String ARTIKEL_NAME_NICHT_VORHANDEN ="Nicht";
	private static final String UPDATE_ARTIKEL_NAME = "Schlafcouch";
	private static final String NEUER_ARTIKEL_NAME = "Gartenhaus";
	private static final String NEUER_ARTIKEL_BESCHREIBUNG = "Das ist ein tolles Gartenhaus";
	private static final Date NEU_ERSTELLT = new GregorianCalendar(2000, 0, 31).getTime();
	
	@Test
	@InSequence(1)
	public void validate() {
		assertThat(true).isTrue();
	}
	
	@Test
	@InSequence(10)
	public void findArtikelById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		
		// When
		Response response = getHttpsClient().target(ARTIKEL_ID_URI)
                                            .resolveTemplate(ArtikelResource.ARTIKEL_ID_PATH_PARAM, artikelId)
                                            .request()
                                            .accept(APPLICATION_JSON)
                                            .get();
	
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId()).isEqualTo(artikelId);
		assertThat(artikel.getName()).isNotEmpty();
		assertThat(artikel.getKategorie()).isNotNull();
		
		// Link-Header fuer Bestellungen pruefen
		assertThat(response.getLinks()).isNotEmpty();
		assertThat(response.getLink(SELF_LINK).getUri().toString()).contains(String.valueOf(artikelId));
				
		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(11)
	public void findArtikelByIdNichtVorhanden(){

		// Given
		final Long artikelId = ARTIKEL_ID_NICHT_VORHANDEN;
		
		// When
		final Response response = getHttpsClient().target(ARTIKEL_ID_URI)
                                                  .resolveTemplate(ArtikelResource.ARTIKEL_ID_PATH_PARAM, artikelId)
                                                  .request()
                                                  .acceptLanguage(GERMAN)
                                                  .get();

    	// Then
    	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Kein Artikel mit der ID \"")
    	                         .endsWith("gefunden.");
		
		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(20)
	public void findArtikelbyName(){
		LOGGER.fine("BEGINN");
		
		//Given
		final String name = ARTIKEL_NAME_VORHANDEN;
		
		//When
		Response response = getHttpsClient().target(ARTIKEL_URI)
										   .queryParam(ArtikelResource.ARTIKEL_NAME_QUERY_PARAM, name)
										   .request()
										   .accept(APPLICATION_JSON)
										   .get();
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		
		final Collection<Artikel> artikel = response.readEntity(new GenericType<Collection<Artikel>>() { });
		
		assertThat(artikel).isNotEmpty()
						   .doesNotContainNull()
						   .doesNotHaveDuplicates();
		
		for(Artikel a : artikel){
			assertThat(a.getName().contains(name));
		}
		
		LOGGER.fine("ENDE");
		
	}
	
	@Test
	@InSequence(21)
	public void findArtikelbyNameNichtVorhanden(){
		LOGGER.fine("BEGINN");
		
		//Given
		final String name = ARTIKEL_NAME_NICHT_VORHANDEN;
		
		//When
		Response response = getHttpsClient().target(ARTIKEL_URI)
										   .queryParam(ArtikelResource.ARTIKEL_NAME_QUERY_PARAM, name)
										   .request()
										   .accept(APPLICATION_JSON)
										   .get();
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).isEqualTo("Keine Artikel mit dem Namen \"" + name + "\" gefunden.");
		
		
		LOGGER.fine("ENDE");
		
	}

	@Test
	@InSequence(30)
	public void createArtikel(){
		
		//Given
		final String name = NEUER_ARTIKEL_NAME;
		final int version = 0;
		final Boolean aufLager = true;
		final String beschreibung = NEUER_ARTIKEL_BESCHREIBUNG;
		final KategorieType kategorie = KategorieType.GARTEN;
		final BigDecimal preis = BigDecimal.valueOf(999,95);
		
		final Artikel artikel = new Artikel();
		artikel.setName(name);
		artikel.setVersion(version);
		artikel.setBeschreibung(beschreibung);
		artikel.setAufLager(aufLager);
		artikel.setErstellt(NEU_ERSTELLT);
		artikel.setPreis(preis);
		artikel.setKategorie(kategorie);
		artikel.setAktualisiert(NEU_ERSTELLT);
		
		Response response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_URI)
											.request()
											.post(json(artikel));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		String location = response.getLocation().toString();
		response.close();
		assertThat(location).isNotEmpty();
		
		LOGGER.finer("ENDE");		
		
	}

	@Test
	@InSequence(31)
	public void createInvalidArtikel(){
		LOGGER.finer("BEGINN");
		
		//Given
		final String name = INVALID_ARTIKEL_NAME;
		final int version = 0;
		final Boolean aufLager = null;
		final String beschreibung = INVALID_ARTIKEL_BESCHREIBUNG;
		final KategorieType kategorie = null;
		final BigDecimal preis = null;
		
		final Artikel artikel = new Artikel();
		artikel.setName(name);
		artikel.setVersion(version);
		artikel.setBeschreibung(beschreibung);
		artikel.setAufLager(aufLager);
		artikel.setErstellt(NEU_ERSTELLT);
		artikel.setPreis(preis);
		artikel.setKategorie(kategorie);
		artikel.setAktualisiert(NEU_ERSTELLT);
		
		Response response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_URI)
				.request()
				.accept(APPLICATION_JSON)
                .acceptLanguage(ENGLISH)
                .post(json(artikel));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
		assertThat(response.getHeaderString("validation-exception")).isEqualTo("true");
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		response.close();
		
		final List<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(violations).isNotEmpty();		
		
		ResteasyConstraintViolation violation =
                filter(violations).with("message")
                                  .equalsTo("The item-name is invalid.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(name));
		
		violation =
                filter(violations).with("message")
                                  .equalsTo("The in stock status has to be true or false.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEmpty();

		violation =
                filter(violations).with("message")
                                  .equalsTo("The description may contain a maximum of 256 characters.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(beschreibung));
		
		violation =
                filter(violations).with("message")
                                  .equalsTo("Category must not be empty.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEmpty();
		
		violation =
                filter(violations).with("message")
                                  .equalsTo("Price must not be emtpy.")
                                  .get()
                                  .iterator()
                                  .next();
		assertThat(violation.getValue()).isEmpty();
		
				LOGGER.finer("ENDE");	
	}

//	@Ignore
	@Test
	@InSequence(40)
	public void updateArtikel(){
		LOGGER.finer("BEGINN");		
		
		//Given
		final Long artikelId = ARTIKEL_ID_UPDATE;
		final String neuerName = UPDATE_ARTIKEL_NAME;
		
		//WHEN 
		Response response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_ID_URI)
											.resolveTemplate(ArtikelResource.ARTIKEL_ID_PATH_PARAM, artikelId)
											.request()
											.accept(APPLICATION_JSON)
											.get();
		
		Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId()).isEqualTo(artikelId);
		final int origVersion = artikel.getVersion();
		
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Namen bauen
		artikel.setName(neuerName);
		
		response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_URI)
								   .request()
								   .accept(APPLICATION_JSON)
								   .put(json(artikel));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getVersion()).isGreaterThan(origVersion);
		
		// Erneutes Update funktioniert, da die Versionsnr. aktualisiert ist
		artikel.setName("update2");
		response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_URI)
		                           .request()
		                           .put(json(artikel));
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();
				
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		artikel.setName("BLA");
		response = getHttpsClient(USERNAME, PASSWORD).target(ARTIKEL_URI)
		                           .request()
		                           .put(json(artikel));
		assertThat(response.getStatus()).isEqualTo(HTTP_CONFLICT);
		response.close();
				
		LOGGER.finer("ENDE");
	}
}
