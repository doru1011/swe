package de.shop.artikelverwaltung.domain;

public enum KategorieType {
	SCHLAFZIMMER ("S"),
	WOHNZIMMER ("W"),
	KUECHE ("KU"),
	BAD ("BA"),
	KINDERZIMMER ("KI"),
	GARTEN ("G"),
	BUERO ("BU");

	private String internal;
	
	private KategorieType(String internal){
		this.internal = internal;
	}
	
	public String getInternal() {
		return internal;
	}
	
	public static KategorieType build(String internal){
		if (internal == null){
			return null;
		}
		
		switch (internal) {
		case "S":
			return SCHLAFZIMMER;
		case "W":
			return WOHNZIMMER;
		case "KU":
			return KUECHE;
		case "BA":
			return BAD;
		case "KI":
			return KINDERZIMMER;
		case "G":
			return GARTEN;
		case "BU":
			return BUERO;
		default:
			throw new IllegalArgumentException(internal + " ist kein gueltiger Wert fuer KategorieType");
				
		}
	}
	
}
