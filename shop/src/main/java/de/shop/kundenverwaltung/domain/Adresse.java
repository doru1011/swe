package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.Index;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.logging.Logger;

@Entity
@XmlRootElement
@Table(indexes = @Index(columnList = "plz"))
public class Adresse implements Serializable {
	private static final long serialVersionUID = -3029272617931844501L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	
	public static final int PLZ_LENGTH_MAX = 5;
	public static final int ORT_LENGTH_MIN = 2;
	public static final int ORT_LENGTH_MAX = 32;
	
	@Id
	@GeneratedValue
	@Column(length = 20, nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{kundenverwaltung.adresse.id.min}")
	private Long id;
	
	@OneToOne
	//NICHT @NotNull, weil beim Anlegen ueber REST der Rueckwaertsverweis noch nicht existiert
	@JoinColumn(name = "kunde_fk", nullable = false, unique = true)
	@XmlTransient
	private Kunde kunde;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(name = "plz", length = 32, nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.plz.notNull}")
	@Pattern(regexp = "\\d{5}", message = "{kundenverwaltung.adresse.plz.digits}")
	private String plz;
	
	@Column(name = "ort", length = 32, nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.ort.notNull}")
	@Size(min = ORT_LENGTH_MIN, max = ORT_LENGTH_MAX, message = "{kundenverwaltung.adresse.ort.length}")
	private String ort;
	
	@Column(nullable = false, updatable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date erstellt;
	
	@Column(nullable = false)
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
		LOGGER.debugf("Neuer Artikel mit ID=%d", id);
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Kunde getKunde() {
		return kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}

	public String getPlz() {
		return plz;
	}
	public void setPlz(String plz) {
		this.plz = plz;
	}
	public String getOrt() {
		return ort;
	}
	public void setOrt(String ort) {
		this.ort = ort;
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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
		result = prime * result + ((ort == null) ? 0 : ort.hashCode());
		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
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
		Adresse other = (Adresse) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null)
				return false;
		} else if (!aktualisiert.equals(other.aktualisiert))
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
		if (ort == null) {
			if (other.ort != null)
				return false;
		} else if (!ort.equals(other.ort))
			return false;
		if (plz == null) {
			if (other.plz != null)
				return false;
		} else if (!plz.equals(other.plz))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Adresse [id=" + id + ", version=" + version + ", plz=" + plz
				+ ", ort=" + ort + ", erstellt=" + erstellt + ", aktualisiert="
				+ aktualisiert + "]";
	}

}
