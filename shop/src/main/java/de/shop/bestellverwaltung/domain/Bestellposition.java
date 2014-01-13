package de.shop.bestellverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;

@Entity
@Table(indexes = { @Index(columnList = "bestellung_fk"), @Index(columnList = "artikel_fk") })
@Cacheable
public class Bestellposition implements Serializable {
	private static final long serialVersionUID = 1031749849939138054L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final int MIN_MENGE = 1;

	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	private Long id;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(nullable = false)
	@Min(value = MIN_MENGE, message = "{bestellverwaltung.bestellposition.menge.min}")
	private Long menge;
	
	@ManyToOne
	@JoinColumn(name = "artikel_fk", nullable = false)
	@XmlTransient
	private Artikel artikel;
	
	@Transient
	private URI artikelUri;
	
	public Bestellposition() {
		super();
	}
	
	public Bestellposition(Artikel artikel) {
		super();
		this.artikel = artikel;
		this.menge = Long.valueOf(1);
	}
	
	public Bestellposition(Artikel artikel, Long menge) {
		super();
		this.artikel = artikel;
		this.menge = menge;
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neue Bestellposition mit ID=%d", id);
	}
	
	@PostUpdate
	private void postUpdate() {
		LOGGER.debugf("Bestellposition mit ID=%s aktualisiert: version=%d", id, version);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Long getMenge() {
		return menge;
	}
	public void setMenge(Long menge) {
		this.menge = menge;
	}
	
	public Artikel getArtikel() {
		return artikel;
	}
	public void setArtikel(Artikel artikel) {
		this.artikel = artikel;
	}

	public URI getArtikelUri() {
		return artikelUri;
	}
	
	public void setArtikelUri(URI artikelUri) {
		this.artikelUri = artikelUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((menge == null) ? 0 : menge.hashCode());
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
		final Bestellposition other = (Bestellposition) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} 
		else if (!id.equals(other.id))
			return false;
		if (menge == null) {
			if (other.menge != null)
				return false;
		} 
		else if (!menge.equals(other.menge))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bestellposition [id=" + id + ", version=" + version
				+ ", menge=" + menge + "]";
	}

	
}

