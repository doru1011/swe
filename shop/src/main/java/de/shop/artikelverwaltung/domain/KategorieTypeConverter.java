package de.shop.artikelverwaltung.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class KategorieTypeConverter implements AttributeConverter<KategorieType, String> {
	@Override
	public String convertToDatabaseColumn(KategorieType KategorieType) {
		if (KategorieType == null) {
			return null;
		}
		return KategorieType.getInternal();
	}

	@Override
	public KategorieType convertToEntityAttribute(String internal) {
		return KategorieType.build(internal);
	}
}
