package de.shop.bestellverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.Index;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.logging.Logger;


@Entity
@XmlRootElement
@Table(indexes = { @Index(columnList = "name"), @Index(columnList = "erstellt") })


@NamedQueries({
//	@NamedQuery(name = Lieferant.FIND_ALL_LIEFERANTEN,
//				query = "SELECT l "
//						+ "FROM Lieferant l "
//						+ " ORDER BY l.id ASC"),
	@NamedQuery(name = Lieferant.FIND_LIEFERANT_BY_NAME,
				query = "Select l "
						+ "FROM Lieferant l "
						+ "WHERE UPPER(l.name) LIKE UPPER(:" + Lieferant.PARAM_LIEFERANT_BEZEICHNUNG
						+ ") ORDER BY l.id ASC"),
	@NamedQuery(name = Lieferant.FIND_LIEFERANT_BY_ID,
				query = "SELECT l "
			        	+ " FROM Lieferant l "
			        	+ " WHERE l.id = :" + Lieferant.PARAM_LIEFERANT_ID)
})

public class Lieferant {	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String PREFIX = "Lieferant.";
	public static final String FIND_LIEFERANT_BY_NAME = PREFIX + "findLieferantByName";
	public static final String FIND_LIEFERANT_BY_ID = PREFIX + "findLieferantById";
	public static final String FIND_ALL_LIEFERANTEN = "findAllLieferanten";
	
	private static final int NAME_MAX_LENGTH = 40;
	private static final int VERSANDKOSTEN_PRECISION = 10;
	private static final int VERSANDKOSTEN_SCALE = 2;
	public static final String PARAM_LIEFERANT_BEZEICHNUNG = "name";
	public static final String PARAM_LIEFERANT_ID = "id";

	

	// TODO OneToMany Bestellung
	
	
	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{bestellverwaltung.lieferant.id.min}")
	private Long id;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(name = "name", length = NAME_MAX_LENGTH, nullable = false)
	@NotNull(message = "{bestellverwaltung.lieferant.name.notNull}")
	@Size(min = 2, message = "{bestellverwaltung.lieferant.name.length}")
	@Pattern(regexp = "[\\w]+", message = "{bestellverwaltung.lieferant.name.pattern}")
	private String name;
	
	@Column(name = "versandkosten", precision = VERSANDKOSTEN_PRECISION, scale = VERSANDKOSTEN_SCALE, nullable = false)
	@NotNull(message = "{bestellverwaltung.lieferant.versandkosten.notNull}")
	private BigDecimal versandkosten;
	
	@Transient
	private URI lieferantUri;
	
	@Column(name = "erstellt", nullable = false, updatable = false)	
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date erstellt;
	
	@Column(name = "aktualisiert")	
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date aktualisiert;
	
	@PrePersist
	private void prePersist() {
		erstellt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neuer Lieferant mit ID=%d", id);
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostUpdate
	private void postUpdate() {
		LOGGER.debugf("Lieferant mit ID=%d aktualisiert", id);
	}	
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public Lieferant() {
		super();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getVersandkosten() {
		return versandkosten;
	}
	public void setVersandkosten(BigDecimal versandkosten) {
		this.versandkosten = versandkosten;
	}
	public URI getLieferantUri() {
		return lieferantUri;
	}
	public void setLieferantUri(URI lieferantUri) {
		this.lieferantUri = lieferantUri;
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
		this.aktualisiert = aktualisiert  == null ? null : (Date) aktualisiert.clone();
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result
				+ ((erstellt == null) ? 0 : erstellt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lieferantUri == null) ? 0 : lieferantUri.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((versandkosten == null) ? 0 : versandkosten.hashCode());
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
		final Lieferant other = (Lieferant) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null)
				return false;
		} 
		else if (!aktualisiert.equals(other.aktualisiert))
			return false;
		if (erstellt == null) {
			if (other.erstellt != null)
				return false;
		} 
		else if (!erstellt.equals(other.erstellt))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} 
		else if (!id.equals(other.id))
			return false;
		if (lieferantUri == null) {
			if (other.lieferantUri != null)
				return false;
		} 
		else if (!lieferantUri.equals(other.lieferantUri))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} 
		else if (!name.equals(other.name))
			return false;
		if (versandkosten == null) {
			if (other.versandkosten != null)
				return false;
		} 
		else if (!versandkosten.equals(other.versandkosten))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Lieferant [id=" + id + ", version=" + version + ", name="
				+ name + ", versandkosten=" + versandkosten + ", lieferantUri="
				+ lieferantUri + ", erstellt=" + erstellt + ", aktualisiert="
				+ aktualisiert + "]";
	}
	
	public void setValues(Lieferant l) {
		version = l.version;
		name = l.name;
		versandkosten = l.versandkosten;	
	}	
}
