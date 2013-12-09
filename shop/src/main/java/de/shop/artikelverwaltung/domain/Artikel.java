package de.shop.artikelverwaltung.domain;

import static de.shop.util.Constants.MIN_ID;
import static de.shop.util.Constants.ERSTE_VERSION;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;



import org.jboss.logging.Logger;

@Entity
@XmlRootElement
@Table(name = "artikel", indexes = { @Index(columnList = "kategorie_fk"), @Index(columnList = "name") })

@NamedQueries({
	@NamedQuery(name = Artikel.FIND_ALL_ARTIKEL,
				query = "SELECT a "
						+ "FROM Artikel a "
						+ "ORDER BY a.id ASC"),
	@NamedQuery(name = Artikel.FIND_ARTIKEL_BY_NAME,
				query = "Select a "
						+ "FROM Artikel a "
						+ "WHERE UPPER(a.name) LIKE UPPER(:" + Artikel.PARAM_NAME
						+ ") ORDER BY a.id ASC"),
	@NamedQuery(name = Artikel.FIND_ARTIKEL_BY_ID,
						query = "Select a "
								+ "FROM Artikel a "
								+ "WHERE a.id = :" + Artikel.PARAM_ID),
	@NamedQuery(name  = Artikel.FIND_LADENHUETER,
				   	    query = "SELECT    a"
				   	            + " FROM   Artikel a"
				   	             + " WHERE  a NOT IN (SELECT bp.artikel FROM Bestellposition bp)")
})
@Cacheable

public class Artikel implements Serializable {
	private static final long serialVersionUID = 469944919370640731L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String PREFIX = "Artikel.";
	public static final String FIND_ALL_ARTIKEL = PREFIX + "findAllArtikel";
	public static final String FIND_ARTIKEL_BY_NAME = PREFIX + "findArtikelByName";
	public static final String FIND_ARTIKEL_BY_ID = PREFIX + "findArtikelByID";
	public static final String FIND_LADENHUETER = PREFIX + "findLadenhueter";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_ID = "id";
	
	private static final int BESCHREIBUNG_MAX_LENGTH = 256;
	private static final int NAME_MAX_LENGTH = 40;
	private static final int PREIS_PRECISION = 10;
	private static final int PREIS_SCALE = 2;
	
	
	//Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer Umlaute:
	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{artikelverwaltung.artikel.id.min}")
	private Long id;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(name = "aufLager", nullable = false)
	@NotNull(message = "{artikelverwaltung.artikel.aufLager.notNull}")
	private Boolean aufLager;
	
	@Column(name = "name", length = NAME_MAX_LENGTH, nullable = false)
	@NotNull(message = "{artikelverwaltung.artikel.name.notNull}")
	@Pattern(regexp = "[\\w]+", message = "{artikelverwaltung.artikel.name.pattern}")
	private String name;
	
	@Column(name = "beschreibung", length = BESCHREIBUNG_MAX_LENGTH)
	@NotNull(message = "{artikelverwaltung.artikel.beschreibung.notNull}")
	@Size(max = BESCHREIBUNG_MAX_LENGTH, message = "{artikelverwaltung.artikel.beschreibung.max}")
	private String beschreibung;
	
	@Column(name = "kategorie_fk", length =2)
	//@Enumerated
	@NotNull(message = "{artikelverwaltung.artikel.kategorie.notNull}")
	private KategorieType kategorie;
	
	@Transient
	private URI artikelUri;
	
	@Column(name = "preis", precision = PREIS_PRECISION, scale = PREIS_SCALE, nullable = false)
	@NotNull(message = "{artikelverwaltung.artikel.preis.notNull}")
	private BigDecimal preis;
	
	@Column(name = "erstellt", nullable = false, updatable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date erstellt;
	
	@Column(name = "aktualisiert")
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date aktualisiert;
	
	@PrePersist
	private void prePersist() {
		erstellt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neuer Artikel mit ID=%d", id);
	}

	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostUpdate
	private void postUpdate() {
		LOGGER.debugf("Artikel mit ID=%d aktualisiert", id);
	}
	
	
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Artikel() {
		super();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getAufLager() {
		return aufLager;
	}
	public void setAufLager(Boolean aufLager) {
		this.aufLager = aufLager;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBeschreibung() {
		return beschreibung;
	}
	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}
	public KategorieType getKategorie() {
		return kategorie;
	}
	public void setKategorie(KategorieType kategorie) {
		this.kategorie = kategorie;
	}
	public URI getArtikelUri() {
		return artikelUri;
	}
	public void setArtikelUri(URI artikelUri) {
		this.artikelUri = artikelUri;
	}
	public BigDecimal getPreis() {
		return preis;
	}
	public void setPreis(BigDecimal preis) {
		this.preis = preis;
	}
	public Date getErstellt() {
		return erstellt == null ? null : (Date) erstellt.clone();
	}
	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
	}
	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}
	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result
				+ ((artikelUri == null) ? 0 : artikelUri.hashCode());
		result = prime * result
				+ ((aufLager == null) ? 0 : aufLager.hashCode());
		result = prime * result
				+ ((beschreibung == null) ? 0 : beschreibung.hashCode());
		result = prime * result
				+ ((erstellt == null) ? 0 : erstellt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((kategorie == null) ? 0 : kategorie.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((preis == null) ? 0 : preis.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artikel other = (Artikel) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null)
				return false;
		} else if (!aktualisiert.equals(other.aktualisiert))
			return false;
		if (artikelUri == null) {
			if (other.artikelUri != null)
				return false;
		} else if (!artikelUri.equals(other.artikelUri))
			return false;
		if (aufLager == null) {
			if (other.aufLager != null)
				return false;
		} else if (!aufLager.equals(other.aufLager))
			return false;
		if (beschreibung == null) {
			if (other.beschreibung != null)
				return false;
		} else if (!beschreibung.equals(other.beschreibung))
			return false;
		if (erstellt == null) {
			if (other.erstellt != null)
				return false;
		} else if (!erstellt.equals(other.erstellt))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (kategorie != other.kategorie)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (preis == null) {
			if (other.preis != null)
				return false;
		} else if (!preis.equals(other.preis))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Artikel [id=" + id + ", version=" + version + ", aufLager="
				+ aufLager + ", name=" + name + ", beschreibung="
				+ beschreibung + ", kategorie=" + kategorie + ", artikelUri="
				+ artikelUri + ", preis=" + preis + ", erstellt=" + erstellt
				+ ", aktualisiert=" + aktualisiert + "]";
	}

	public void setValues(Artikel a) {
		// TODO Auto-generated method stub
		version = a.version;
		name = a.name;
		kategorie = a.kategorie;
		beschreibung = a.beschreibung;
		preis = a.preis;
		aufLager = a.aufLager;
		
		
	}

	
}
