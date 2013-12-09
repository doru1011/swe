package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.TIMESTAMP;
import static javax.persistence.TemporalType.DATE;
import static de.shop.util.Constants.MIN_ID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;
import org.jboss.logging.Logger;

import de.shop.auth.domain.RolleType;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.persistence.File;


@Entity
@XmlRootElement
@Table(name = "kunde", indexes = { @Index(columnList = "nachname"), @Index(columnList = "file_fk")})

@NamedQueries({
	@NamedQuery(name = Kunde.FIND_KUNDEN,
				query = "SELECT k"
						+ " FROM Kunde k"),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME,
			query = "Select k "
					+ "FROM Kunde k "
					+ "WHERE UPPER(k.nachname) LIKE UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME
					+ ") ORDER BY k.id ASC"),
	@NamedQuery(name  = Kunde.FIND_NACHNAMEN_BY_PREFIX,
	query = "SELECT   DISTINCT k.nachname"
					+ " FROM  Kunde k "
	   	            + " WHERE UPPER(k.nachname) LIKE UPPER(:"
	   	           + Kunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID,
		    query = "SELECT   k"
		    		+ " FROM  Kunde k"
		            + " WHERE CONCAT('', k.id) = :" + Kunde.PARAM_KUNDE_USERNAME),
    @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_ID,
                    query = "SELECT k"
        			        + " FROM   Kunde k"
                    		+ " WHERE k.id = :" + Kunde.PARAM_KUNDE_ID),
   	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL,
       query = "SELECT DISTINCT k"
	            + " FROM   Kunde k"
	            + " WHERE  k.email = :" + Kunde.PARAM_KUNDE_EMAIL),
	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
    query = "SELECT DISTINCT k"
            + " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
            + " WHERE  k.id = :" + Kunde.PARAM_KUNDE_ID),
    @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
    query = "SELECT      DISTINCT k"
		      + " FROM     Kunde k LEFT JOIN FETCH k.bestellungen"
		      + " WHERE    UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"
		      + " ORDER BY k.id"),
    @NamedQuery(name  = Kunde.FIND_KUNDE_BY_USERNAME,
    query = "SELECT   k"
	        + " FROM  Kunde k"
       		+ " WHERE CONCAT('', k.username) = :" + Kunde.PARAM_KUNDE_USERNAME),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_ORDER_BY_USERNAME,
    query = "SELECT   k"
	        + " FROM  Kunde k"
            + " ORDER BY k.username"),
    @NamedQuery(name  = Kunde.FIND_USERNAME_BY_USERNAME_PREFIX,
	            query = "SELECT   CONCAT('', k.id)"
				        + " FROM  Kunde k"
	            		+ " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_USERNAME_PREFIX),
    @NamedQuery(name = Kunde.FIND_KUNDE_BY_BESTELLUNG_ID,
    		query = "SELECT k FROM Kunde k JOIN k.bestellungen b WHERE b.id = :" + Kunde.PARAM_BESTELLUNG_ID)
})
@NamedEntityGraph(name = Kunde.GRAPH_BESTELLUNGEN,
					  attributeNodes = @NamedAttributeNode("bestellungen"))

@ScriptAssert(lang = "javascript",
script = "_this.password != null && !_this.password.equals(\"\")"
		   + "&& _this.password.equals(_this.passwordWdh)",
message = "{kundenverwaltung.kunde.password.notEqual}",
groups = { Default.class, PasswordGroup.class })

public class Kunde implements Serializable {
	private static final long serialVersionUID = 7401524595142572933L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String PREFIX = "Kunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
	public static final String FIND_KUNDE_BY_ID = PREFIX + "findKundeById";
	public static final String FIND_KUNDEN_BY_ID = PREFIX + "findKundenById";
	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN = PREFIX + "findKundeByIdFetchBestellungen";
	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN =
            PREFIX + "findKundenByNachnameFetchBestellungen";
	public static final String FIND_KUNDE_BY_USERNAME = PREFIX + "findKundeByUserName";
	public static final String FIND_KUNDEN_ORDER_BY_USERNAME = PREFIX + "findKundenOrderByUsername";
	public static final String FIND_KUNDE_BY_BESTELLUNG_ID = PREFIX + "findKundeByBestellungId";
	public static final String FIND_USERNAME_BY_USERNAME_PREFIX = PREFIX + "findKundeByUsernamePrefix";
	public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX + "findNachnamenByPrefix";
	
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_ID = "id";
	public static final String PARAM_KUNDE_EMAIL = "email";
	public static final String PARAM_KUNDE_USERNAME = "username";
	public static final String PARAM_BESTELLUNG_ID = "bestellungId";
	public static final String PARAM_USERNAME_PREFIX = "usernamePrefix";
	public static final String GRAPH_BESTELLUNGEN = "bestellungen";
	public static final String PARAM_KUNDE_NACHNAME_PREFIX = "nachnamePrefix";
	
	//Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer Umlaute:
	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
	private static final String NACHNAME_PREFIX = "(o'|von|von der|von und zu|van)?";
	
	public static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
	
	public static final String NACHNAME_PATTERN = NACHNAME_PREFIX + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	public static final int NACHNAME_LENGTH_MIN = 2;
	public static final int NACHNAME_LENGTH_MAX = 32;
	private static final int VORNAME_LENGTH_MAX = 32;
	public static final int EMAIL_LENGTH_MAX = 128;
	public static final int PASSWORD_LENGTH_MAX = 256;
	
		
	@Column(nullable = false, updatable = false)
	@Pattern(regexp = USERNAME_PATTERN, message = "{kunde.username.pattern}" )
	private String username;
	
	@Id
	@Column(nullable =false, updatable = false)
	@GeneratedValue
	@Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}")
	private Long id;

	@Column(nullable = false, length = PASSWORD_LENGTH_MAX)
	@Size(max = PASSWORD_LENGTH_MAX, message = "{kundenverwaltung.kunde.password.length}")
	private String password;
	
	@Transient
	@Size(max = PASSWORD_LENGTH_MAX, message = "{kundenverwaltung.kunde.password.length}")
	private String passwordWdh;
	
	@Basic(optional = false)
	@Temporal(DATE)
	@Past(message = "{kunde.seit.past}")
	private Date seit;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(name = "nachname", length = NACHNAME_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.nachname.length}")
	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
	private String nachname;
	
	@Column(length = VORNAME_LENGTH_MAX)
	@Size(max = VORNAME_LENGTH_MAX, message = "{kunde.vorname.length}")
	private String vorname = "";
	
	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
	@Size(max = EMAIL_LENGTH_MAX, message = "{kundenverwaltung.kunde.email.length}")
	private String email;
	
	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "file_fk")
	@XmlTransient
	private File file;	
	
	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_rolle", 
		joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
		uniqueConstraints = @UniqueConstraint(columnNames = { "kunde_fk", "rolle"}))
	@Column(table = "kunde_rolle", name = "rolle", length = 32, nullable = false)
	private Set<RolleType> rollen;
	
	@Column(nullable = false, updatable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	@Past(message = "{kundenverwaltung.kunde.erstellt.past}")
	private Date erstellt;
	
	@JsonIgnore
	@Past(message = "{kundenverwaltung.kunde.aktualisiert.past}")
	private Date aktualisiert;
	
	@Transient
	@AssertTrue(message = "{kunde.agb}")
	private boolean agbAkzeptiert;
	
	@OneToOne(mappedBy = "kunde", cascade = { PERSIST, REMOVE })
	@Valid
//	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	private Adresse adresse;
	
	@OneToMany (fetch = LAZY)
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@JsonIgnore
	private List<Bestellung> bestellungen;
	
	@Transient
	private URI bestellungenUri;
	
	
	
	
	@PrePersist
	protected void prePersist() {
		erstellt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	protected void postPersist() {	
		LOGGER.debugf("Neuer Kunde mit Username= %d", username);
	}
	
	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostUpdate
	protected void postUpdate() {	
		LOGGER.debugf("Kunde %d aktualisiert.", username);
	}
	
	@PostLoad
	protected void postLoad() {
		passwordWdh = password;
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getVorname() {
		return vorname;
	}
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Set<RolleType> getRollen() {
		if (rollen == null) {
			return null;
		}
		
		return Collections.unmodifiableSet(rollen);
	}

	public void setRollen(Set<RolleType> rollen) {
		if (this.rollen == null) {
			this.rollen = rollen;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.rollen.clear();
		if (rollen != null) {
			this.rollen.addAll(rollen);
		}
	}

	public String getNachname() {
		return nachname;
	}
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getErstellt() {
		return erstellt == null ? null : (Date) erstellt.clone();
	}
	public void setSeit(Date seit) {
		this.erstellt = seit == null ? null : (Date) erstellt.clone();
	}
	public Adresse getAdresse() {
		return adresse;
	}
	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}
	public Date getAktualisiert() {
		return aktualisiert;
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert;
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt;
	}
	
	public boolean isAgbAkzeptiert() {
		return agbAkzeptiert;
	}

	public List<Bestellung> getBestellungen() {
		return bestellungen;
	}
	public void setBestellungen(List<Bestellung> bestellungen) {
		this.bestellungen = bestellungen;
	}
	public URI getBestellungenUri() {
		return bestellungenUri;
	}
	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}
	
	public Kunde addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<>();
		}
		bestellungen.add(bestellung);
		return this;
	}
	
	public void setValues(Kunde k) {
		version = k.version;
		username = k.username;
		nachname = k.nachname;
		vorname = k.vorname;
		email = k.email;
		password = k.password;
		passwordWdh = k.password;
		erstellt = k.erstellt;
		agbAkzeptiert = k.agbAkzeptiert;
	}

	
	public Kunde addRollen(Collection<RolleType> rollen) {
		LOGGER.tracef("neue Rollen: %s", rollen);
		if (this.rollen == null) {
			this.rollen = new HashSet<>();
		}
		this.rollen.addAll(rollen);
		LOGGER.tracef("Rollen nachher: %s", this.rollen);
		return this;
	}
	
	public Kunde removeRollen(Collection<RolleType> rollen) {
		LOGGER.tracef("zu entfernende Rollen: %s", rollen);
		if (this.rollen == null) {
			return this;
		}
		this.rollen.removeAll(rollen);
		LOGGER.tracef("Rollen nachher: %s", this.rollen);
		return this;
	}

	@Override
	public String toString() {
		return "Kunde [username=" + username + ", id=" + id + ", password="
				+ password + ", passwordWdh=" + passwordWdh + ", version="
				+ version + ", nachname=" + nachname + ", vorname=" + vorname + ", seit="
				+", email=" + email
				+ ", erstellt=" + erstellt
				+ ", aktualisiert=" + aktualisiert + ", bestellungenUri="
				+ bestellungenUri + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result
				+ ((bestellungenUri == null) ? 0 : bestellungenUri.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((erstellt == null) ? 0 : erstellt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((nachname == null) ? 0 : nachname.hashCode());
		result = prime * result
				+ ((vorname == null) ? 0 : vorname.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((passwordWdh == null) ? 0 : passwordWdh.hashCode());
		result = prime * result + ((rollen == null) ? 0 : rollen.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		Kunde other = (Kunde) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null)
				return false;
		} else if (!aktualisiert.equals(other.aktualisiert))
			return false;
		if (bestellungenUri == null) {
			if (other.bestellungenUri != null)
				return false;
		} else if (!bestellungenUri.equals(other.bestellungenUri))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
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
		if (nachname == null) {
			if (other.nachname != null)
				return false;
		} else if (!nachname.equals(other.nachname))
			return false;
		if (vorname == null) {
			if (other.vorname != null)
				return false;
		} else if (!vorname.equals(other.vorname))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (passwordWdh == null) {
			if (other.passwordWdh != null)
				return false;
		} else if (!passwordWdh.equals(other.passwordWdh))
			return false;
//		if (rollen == null) {
//			if (other.rollen != null)
//				return false;
//		} else if (!rollen.equals(other.rollen))
//			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

}
