package de.shop.kundenverwaltung.rest;

import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.KUNDEN_ID_FILE_URI;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.USERNAME;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.invoke.MethodHandles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.client.Entity.entity;


import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.auth.domain.RolleType;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.AbstractResourceTest;


//Logging durch java.util.logging

@RunWith(Arquillian.class)
public class KundeResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long KUNDE_ID_VORHANDEN_MIT_BESTELLUNGEN = Long.valueOf(101);
	private static final Long KUNDE_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long KUNDE_ID_UPDATE = Long.valueOf(101);

	private static final String NACHNAME_VORHANDEN = "Dirkson";
	private static final String NACHNAME_NICHT_VORHANDEN = "Falschername";
	private static final String NACHNAME_INVALID = "1_%$][]��4�$Test9";
	private static final String NEUER_USERNAME = "Gustav";
	private static final String NEUER_NACHNAME = "Nachnameneu";
	private static final String NEUER_NACHNAME_INVALID = "!";
	private static final String NEUER_VORNAME = "Vorname";
	private static final String NEUE_EMAIL = NEUER_NACHNAME + "@test.de";
	private static final String NEUE_EMAIL_INVALID = "?";
	private static final short NEUE_KATEGORIE = 1;
	private static final Date NEU_ERSTELLT = new GregorianCalendar(2000, 0, 31).getTime();
	private static final String NEUE_PLZ = "76133";
	private static final String NEUE_PLZ_FALSCH = "2";
	private static final String NEUER_ORT = "Karlsruhe";
	private static final String NEUES_PASSWORD = "neuesPassword";
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(300);
	
	private static final String IMAGE_FILENAME = "image2.jpg";
	private static final String IMAGE_PATH_UPLOAD = "src/test/resources/rest/" + IMAGE_FILENAME;
	private static final String IMAGE_MIMETYPE = "image/jpg";
	private static final String IMAGE_PATH_DOWNLOAD = "target/" + IMAGE_FILENAME;
	private static final Long KUNDE_ID_UPLOAD = Long.valueOf(102);

	private static final String IMAGE_INVALID = "image.bmp";
	private static final String IMAGE_INVALID_PATH = "src/test/resources/rest/" + IMAGE_INVALID;
	private static final String IMAGE_INVALID_MIMETYPE = "image/bmp";
	
	
	@Test
	@InSequence(1)
	public void validate() {
		assertThat(true).isTrue();
	}
	
	@Test
	@InSequence(10)
	public void findKundeMitBestellungenById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long kundeId = KUNDE_ID_VORHANDEN_MIT_BESTELLUNGEN;
		
		// When
		Response response = getHttpsClient().target(KUNDEN_ID_URI)
                                            .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
                                            .request()
                                            .accept(APPLICATION_JSON)
                                            .get();
	
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Kunde kunde = response.readEntity(Kunde.class);
		assertThat(kunde.getId()).isEqualTo(kundeId);
		assertThat(kunde.getNachname()).isNotEmpty();
		assertThat(kunde.getAdresse()).isNotNull();
		
		// Link-Header fuer Bestellungen pruefen
		assertThat(response.getLinks()).isNotEmpty();
		assertThat(response.getLink(SELF_LINK).getUri().toString()).contains(String.valueOf(kundeId));
		
		final URI bestellungenUri = kunde.getBestellungenUri();
		assertThat(bestellungenUri).isNotNull();
		
		response = getHttpsClient().target(bestellungenUri)
				                   .request()
				                   .accept(APPLICATION_JSON)
				                   .get();
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		
		// Verweist der Link-Header der ermittelten Bestellungen auf den Kunden?
		final Collection<Bestellung> bestellungen = response.readEntity(new GenericType<Collection<Bestellung>>() { });
		
		assertThat(bestellungen).isNotEmpty()
		                        .doesNotContainNull()
		                        .doesNotHaveDuplicates();
		for (Bestellung b : bestellungen) {
			assertThat(b.getKundeUri().toString()).endsWith(String.valueOf(kundeId));			
		}
		
		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(11)
	public void findKundeByIdNichtVorhanden() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long kundeId = KUNDE_ID_NICHT_VORHANDEN;
		
		// When
		final Response response = getHttpsClient().target(KUNDEN_ID_URI)
                                                  .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
                                                  .request()
                                                  .acceptLanguage(GERMAN)
                                                  .get();

    	// Then
    	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Kein Kunde mit der ID")
    	                         .endsWith("gefunden.");
		
		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(20)
	public void findKundenByNachnameVorhanden() {
		LOGGER.fine("BEGINN");
		
		//Given
		final String nachname = NACHNAME_VORHANDEN;
		
		//When
		Response response = getHttpsClient().target(KUNDEN_URI)
										   .queryParam(KundeResource.KUNDEN_NACHNAME_QUERY_PARAM, nachname)
										   .request()
										   .accept(APPLICATION_JSON)
										   .get();
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		
		final Collection<Kunde> kunden = response.readEntity(new GenericType<Collection<Kunde>>() { });
		
		assertThat(kunden).isNotEmpty()
						   .doesNotContainNull()
						   .doesNotHaveDuplicates();
		
		for (Kunde k : kunden) {
			assertThat(k.getNachname().contains(nachname));
		}
		
		LOGGER.fine("ENDE");
	}
	
	@Test
	@InSequence(21)
	public void findKundenByNachnameNichtVorhanden() {
		LOGGER.finer("BEGINN");
		
		// Given
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		
		// When
		final Response response = getHttpsClient().target(KUNDEN_URI)
                                                  .queryParam(KundeResource.KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                                  .request()
                                                  .acceptLanguage(GERMAN)
                                                  .get();
		
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
		final String fehlermeldung = response.readEntity(String.class);
		assertThat(fehlermeldung).isEqualTo("Keine Kunden mit dem Namen \"" + nachname + "\" gefunden.");

		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(22)
	public void findKundenByNachnameInvalid() {
		LOGGER.finer("BEGINN");
		
		// Given
		final String nachname = NACHNAME_INVALID;
		
		// When
		final Response response = getHttpsClient().target(KUNDEN_URI)
                                                  .queryParam(KundeResource.KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                                  .request()
                                                  .accept(APPLICATION_JSON)
                                                  .acceptLanguage(ENGLISH)
                                                  .get();
		
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
		assertThat(response.getHeaderString("validation-exception")).isEqualTo("true");
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		final List<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(violations).isNotEmpty();
		
		final ResteasyConstraintViolation violation =
				                          filter(violations).with("message")
                                                            .equalsTo("Invalid lastname. A valid lastname starts with a capital letter end does not contain any special characters.")
                                                            .get()
                                                            .iterator()
                                                            .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(nachname));

		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(40)
	public void createKunde() throws URISyntaxException {
		LOGGER.finer("BEGINN");
		
		// Given
		final String username = NEUER_USERNAME;
		final String nachname = NEUER_NACHNAME;
		final String email = NEUE_EMAIL;	
		final String plz = NEUE_PLZ;
		final String ort = NEUER_ORT;	
		final String neuesPassword = NEUES_PASSWORD;
		
		final Kunde kunde = new Kunde();
		kunde.setNachname(nachname);
		kunde.setEmail(email);
		kunde.setUsername(username);
		final Adresse adresse = new Adresse();
		adresse.setOrt(ort);
		adresse.setPlz(plz);
		kunde.setAdresse(adresse);
		kunde.setPassword(neuesPassword);
		kunde.setPasswordWdh(neuesPassword);
		kunde.addRollen(Arrays.asList(RolleType.KUNDE, RolleType.MITARBEITER));
		
		Response response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_URI)
                                            .request()
                                            .post(json(kunde));
			
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id).isPositive();
		
		// Given (2)
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		final String neuerusername = idStr;

		// When (2)
		final Bestellung bestellung = new Bestellung();
		final Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId));
		bp.setMenge(Long.valueOf(1));
		bestellung.addBestellposition(bp);
		
		// Then (2)
		response = getHttpsClient(neuerusername, neuesPassword).target(BESTELLUNGEN_URI)
                                                          .request()
                                                          .post(json(bestellung));

		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		location = response.getLocation().toString();
		response.close();
		assertThat(location).isNotEmpty();

		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(41)
	public void createKundeInvalid() {
		LOGGER.finer("BEGINN");
		
		// Given
		final String nachname = NACHNAME_INVALID;
		final String username = NEUER_USERNAME;
		final String email = NEUE_EMAIL_INVALID;
		final Date erstellt = NEU_ERSTELLT;
		final String password = NEUES_PASSWORD;
		final String passwordWdh = NEUES_PASSWORD;
		final String plz = NEUE_PLZ_FALSCH;
		final String ort = NEUER_ORT;
		final Kunde kunde = new Kunde();
		kunde.setNachname(nachname);
		kunde.setUsername(username);
		kunde.setEmail(email);
		kunde.setErstellt(erstellt);
		kunde.setPassword(password);
		kunde.setPasswordWdh(passwordWdh);
		final Adresse adresse = new Adresse();
		adresse.setPlz(plz);
		adresse.setOrt(ort);
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		// When
		final Response response = getHttpsClient().target(KUNDEN_URI)
                                                                    .request()
                                                                    .accept(APPLICATION_JSON)
                                                                    // engl. Fehlermeldungen ohne Umlaute 
                                                                    .acceptLanguage(ENGLISH)
                                                                    .post(json(kunde));
		
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
		assertThat(response.getHeaderString("validation-exception")).isEqualTo("true");
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		response.close();
		
		final List<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(violations).isNotEmpty();
		
		ResteasyConstraintViolation violation =
				                    filter(violations).with("message")
                                                      .equalsTo("Invalid lastname. A valid lastname starts with a capital letter end does not contain any special characters.")
                                                      .get()
                                                      .iterator()
                                                      .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(nachname));
		
		violation = filter(violations).with("message")
                                      .equalsTo("Invalid lastname. A valid lastname starts with a capital letter end does not contain any special characters.")
                                      .get()
                                      .iterator()
                                      .next();
		assertThat(violation.getValue()).isEqualTo(String.valueOf(nachname));

		violation = filter(violations).with("message")
				                      .equalsTo("The email address is not valid.")
				                      .get()
				                      .iterator()
				                      .next();
		assertThat(violation.getValue()).isEqualTo(email);
		
//		
//		violation = filter(violations).with("message")
//                                      .equalsTo("Passwords are not equal.")
//                                      .get()
//                                      .iterator()
//                                      .next();
//		 @ScriptAssert steht bei der Klasse und nicht bei einem Attribut:
//		 violation.getValue() ruft toString() auf dem Objekt der Klasse Privatkunde auf
//		assertThat(violation.getValue()).contains(password).contains(passwordWdh);	
		
		
		violation = filter(violations).with("message")
                                      .equalsTo("The ZIP code doesn't have 5 digits.")
                                      .get()
                                      .iterator()
                                      .next();
		assertThat(violation.getValue()).isEqualTo(plz);
		
		LOGGER.finer("ENDE");
	}
//
	@Test
	@InSequence(50)
	public void updateKunde() {
		LOGGER.finer("BEGINN");
		//TODO Username und Passwort in getHttpsClients einf�gen
		
		//Given
		final Long kundeId = KUNDE_ID_UPDATE;
		final String neuerNachname = "BLA";
		
		//WHEN 
		Response response = getHttpsClient().target(KUNDEN_ID_URI)
											.resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
											.request()
											.accept(APPLICATION_JSON)
											.get();
		
		Kunde kunde = response.readEntity(Kunde.class);
		assertThat(kunde.getId()).isEqualTo(kundeId);
		final int origVersion = kunde.getVersion();
		
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Namen bauen
		kunde.setNachname(neuerNachname);
		
		response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_URI)
								   .request()
								   .accept(APPLICATION_JSON)
								   .put(json(kunde));
		
		//Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		kunde = response.readEntity(Kunde.class);
		assertThat(kunde.getVersion()).isGreaterThan(origVersion);
		
		// Erneutes Update funktioniert, da die Versionsnr. aktualisiert ist
		kunde.setNachname("update2");
		response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_URI)
		                           .request()
		                           .put(json(kunde));
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();
				
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		kunde.setNachname("BLA");
		response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_URI)
		                           .request()
		                           .put(json(kunde));
		assertThat(response.getStatus()).isEqualTo(HTTP_CONFLICT);
		response.close();
				
		LOGGER.finer("ENDE");
	}
	
//	@Test
//	@InSequence(60)
//	public void deleteKunde() {
//		LOGGER.finer("BEGINN");
//		
//		// Given
//		final Long kundeId = KUNDE_ID_DELETE;
//		
//		// When
//		Response response = getHttpsClient().target(KUNDEN_ID_URI)
//                                            .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
//                                            .request()
//                                            .accept(APPLICATION_JSON)
//                                            .get();
//		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
//		response.close();
//		
//		response = getHttpsClient(USERNAME_ADMIN, PASSWORD_ADMIN).target(KUNDEN_ID_URI)
//                                                                 .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM,
//                                                                		          kundeId)
//                                                                 .request()
//                                                                 .delete();
//		
//		// Then
//		assertThat(response.getStatus()).isEqualTo(HTTP_NO_CONTENT);
//		response.close();
//		
//		response = getHttpsClient().target(KUNDEN_ID_URI)
//                                   .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
//                                   .request()
//                                   .accept(APPLICATION_JSON)
//                                   .get();
//       	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
//		response.close();
//        
//		LOGGER.finer("ENDE");
//	}
//	
//	@Test
//	@InSequence(61)
//	public void deleteKundeMitBestellung() {
//		LOGGER.finer("BEGINN");
//		
//		// Given
//		final Long kundeId = KUNDE_ID_DELETE_MIT_BESTELLUNGEN;
//		
//		// When
//		final Response response =
//				       getHttpsClient(USERNAME_ADMIN, PASSWORD_ADMIN).target(KUNDEN_ID_URI)
//                                                                   .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM,
//                                                                                    kundeId)
//                                                                   .request()
//                                                                   .acceptLanguage(GERMAN)
//                                                                   .delete();
//		
//		// Then
//		assertThat(response.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
//		final String fehlermeldung = response.readEntity(String.class);
//		assertThat(fehlermeldung).startsWith("Der Kunde mit ID")
//		                         .endsWith("Bestellung(en).");
//		
//		LOGGER.finer("ENDE");
//	}
//	
//	
//	@Test
//	@InSequence(62)
//	public void deleteKundeFehlendeBerechtigung() {
//		LOGGER.finer("BEGINN");
//		
//		// Given
//		final Long kundeId = KUNDE_ID_DELETE_FORBIDDEN;
//		
//		// When
//		final Response response =
//                       getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_ID_URI)
//                                                         .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
//                                                         .request()
//                                                         .delete();
//		
//		// Then
//		assertThat(response.getStatus()).isIn(HTTP_FORBIDDEN, HTTP_NOT_FOUND);
//		response.close();
//		
//		LOGGER.finer("ENDE");
//	}
//	
	@Test
	@InSequence(70)
	public void uploadDownload() throws IOException {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long kundeId = KUNDE_ID_UPLOAD;
		final String path = IMAGE_PATH_UPLOAD;
		final String mimeType = IMAGE_MIMETYPE;
		
		// Datei einlesen
		final byte[] uploadBytes = Files.readAllBytes(Paths.get(path));
		
		// When
		Response response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_ID_FILE_URI)
                .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM,
              		           kundeId)
                .request()
                .post(entity(uploadBytes, mimeType));
		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		// id extrahieren aus http://localhost:8080/shop/rest/kunden/<id>/file
		final String location = response.getLocation().toString();
		response.close();
		
		final String idStr = location.replace(KUNDEN_URI + '/', "")
				                     .replace("/file", "");
		assertThat(idStr).isEqualTo(kundeId.toString());
		
		// When (2)
		// Download der zuvor hochgeladenen Datei
		byte[] downloadBytes;
		
		response = getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_ID_FILE_URI)
                                                     .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
                                                     .request()
                                                     .accept(mimeType)
                                                     .get();
		downloadBytes = response.readEntity(new GenericType<byte[]>() { });
		
		// Then (2)
		assertThat(uploadBytes.length).isEqualTo(downloadBytes.length);
		assertThat(uploadBytes).isEqualTo(downloadBytes);
		
		// Abspeichern des heruntergeladenen byte[] als Datei im Unterverz. target zur manuellen Inspektion
		Files.write(Paths.get(IMAGE_PATH_DOWNLOAD), downloadBytes);
		LOGGER.info("Heruntergeladene Datei abgespeichert: " + IMAGE_PATH_DOWNLOAD);
		
		LOGGER.finer("ENDE");
	}
//	
//	@Test
//	@InSequence(71)
//	public void uploadInvalidMimeType() throws IOException {
//		LOGGER.finer("BEGINN");
//		
//		// Given
//		final Long kundeId = KUNDE_ID_UPLOAD;
//		final String path = IMAGE_INVALID_PATH;
//		final String mimeType = IMAGE_INVALID_MIMETYPE;
//		
//		// Datei einlesen
//		final byte[] uploadBytes = Files.readAllBytes(Paths.get(path));
//		
//		// When
//		final Response response =
//				       getHttpsClient(USERNAME, PASSWORD).target(KUNDEN_ID_FILE_URI)
//                                                         .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kundeId)
//                                                         .request()
//                                                         .post(entity(uploadBytes, mimeType));
//		
//		assertThat(response.getStatus()).isEqualTo(HTTP_UNSUPPORTED_TYPE);
//		response.close();
//	}	
}
