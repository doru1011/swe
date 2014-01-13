package de.shop.bestellverwaltung.domain;

import static de.shop.util.Constants.*; 
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.Index;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.logging.Logger;

import de.shop.kundenverwaltung.domain.Kunde;

@Entity
@Table(indexes = { @Index(columnList = "kunde_fk"), @Index(columnList = "erstellt") })
@NamedQueries({
	@NamedQuery(name  = Bestellung.FIND_BESTELLUNGEN_BY_KUNDE_ID,
                query = "SELECT b"
			            + " FROM   Bestellung b"
						+ " WHERE  b.kunde.id = :" + Bestellung.PARAM_ID),
   	@NamedQuery(name  = Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_BESTELLPOSITIONEN,
			    query = "SELECT DISTINCT b"
                        + " FROM   Bestellung b LEFT JOIN FETCH b.bestellpositionen"
   			            + " WHERE  b.id = :" + Bestellung.PARAM_ID),
    @NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_ID,
    			query = "SELECT b"
    					+ " FROM Bestellung b"
    					+ " WHERE b.id = :" + Bestellung.PARAM_ID)

})

@XmlRootElement
@Cacheable
public class Bestellung implements Serializable {
	private static final long serialVersionUID = 1618359234119003714L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String PREFIX = "Bestellung.";
	public static final String FIND_BESTELLUNGEN_BY_KUNDE_ID = PREFIX + "findBestellungenByKundeId";
	public static final String FIND_BESTELLUNG_BY_ID_FETCH_BESTELLPOSITIONEN =
		                       PREFIX + "findBestellungenByIdFetchBESTELLPOSITIONEN";
	public static final String FIND_BESTELLUNG_BY_ID = PREFIX + "findBestellungById";
	public static final String FIND_KUNDE_BY_USERNAME = PREFIX + "findBestellungKundeByUsername";
	public static final String PARAM_KUNDE = "kunde";
	public static final String PARAM_ID = "id";
	public static final String PARAM_KUNDE_ID = "kundeId";
	public static final String PARAM_USERNAME = "username";
	public static final String FIND_KUNDE_BY_BESTELLUNG_ID = PREFIX + "findKundeByBestellungId";
	
	@Id
	@GeneratedValue
	@Column(length = 20, nullable = false, updatable = false)
	private Long id = KEINE_ID;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@OneToMany(fetch = EAGER, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "bestellung_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@NotEmpty(message = "{bestellverwaltung.bestellung.bestellpositionen.notEmpty}")
	@Valid
	private List<Bestellposition> bestellpositionen;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	@XmlTransient
	private Kunde kunde;
	
	@Transient
	private URI kundeUri;
	
	 //TODO Set <Lieferant> verweis auf lieferant.... ala jürgen, erstmal commented
//	@ManyToMany
//	@JoinTable(name = "bestellung_lieferant",
//			   joinColumns = @JoinColumn(name = "bestellung_fk"),
//			   inverseJoinColumns = @JoinColumn(name = "lieferant_fk"))
//	@XmlTransient
//	private Set<Lieferant> lieferant;
	
	@Column(nullable = false, updatable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date erstellt;
	
	@Basic(optional = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date aktualisiert;
	
	@Basic(optional = true)
	private boolean ausgeliefert;
	
	public Bestellung() {
		super();
	}
	
	public Bestellung(Kunde kunde, List<Bestellposition> bestellpositionen) {
		super();
		this.kunde = kunde;
		this.bestellpositionen = bestellpositionen;
	
	}
	
	@PrePersist
	private void prePersist() {
		erstellt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neue Bestellung mit ID=%d", id);
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isAusgeliefert() {
		return ausgeliefert;
	}
	public void setAusgeliefert(boolean ausgeliefert) {
		this.ausgeliefert = ausgeliefert;
	}
	public Kunde getKunde() {
		return kunde;
	}
	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
	
	public URI getKundeUri() {
		return kundeUri;
	}
	public void setKundeUri(URI kundeUri) {
		this.kundeUri = kundeUri;
	}


	public List<Bestellposition> getBestellpositionen() {
		if (bestellpositionen == null) {
			return null;
		}
		
		return Collections.unmodifiableList(bestellpositionen);
	}
	
	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		if (this.bestellpositionen == null) {
			this.bestellpositionen = bestellpositionen;
			return;
			}
			// Wiederverwendung der vorhandenen Collection
			this.bestellpositionen.clear();
			if (bestellpositionen != null) {
			this.bestellpositionen.addAll(bestellpositionen);
			}
	}
	
	
	public Bestellung addBestellposition(Bestellposition bestellposition) {
		if (bestellpositionen == null) {
			bestellpositionen = new ArrayList<>();
		}
		bestellpositionen.add(bestellposition);
		return this;
	}
	
	
	public Date getErstellt() {
		return erstellt == null ? null : (Date) erstellt.clone();
	}
	
	public String getErstellt(String format) {
		final Format formatter = new SimpleDateFormat(format, Locale.getDefault());
		return formatter.format(erstellt);
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

	//TODO verweis auf Lieferanten
//	public Set<Lieferant> getLieferungen() {
//		return lieferant == null ? null : Collections.unmodifiableSet(lieferant);
//	}
//
//	public void setLieferungen(Set<Lieferant> lieferungen) {
//		if (this.lieferant == null) {
//			this.lieferant = lieferungen;
//			return;
//		}
//		
//		// Wiederverwendung der vorhandenen Collection
//		this.lieferant.clear();
//		if (lieferungen != null) {
//			this.lieferant.addAll(lieferungen);
//		}
//	}
//	
//	@XmlTransient
//	public List<Lieferant> getLieferantAsList() {
//		 return lieferant == null ? null : new ArrayList<>(lieferant);
//	}
//	
//	public void setLieferantAsList(List<Lieferant> lieferungen) {
//		this.lieferant = lieferant == null ? null : new HashSet<>(lieferant);
//	}	
//	
	
	
	@Override
	public String toString() {
		return "Bestellung [id=" + id + ", ausgeliefert=" + ausgeliefert
				+ ", erstellt=" + erstellt + ", aktualisiert=" + aktualisiert
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result + (ausgeliefert ? 1231 : 1237);
		result = prime * result
				+ ((erstellt == null) ? 0 : erstellt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		final Bestellung other = (Bestellung) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null)
				return false;
		}
		else if (!aktualisiert.equals(other.aktualisiert))
			return false;
		if (ausgeliefert != other.ausgeliefert)
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
		return true;
	}
	
}
