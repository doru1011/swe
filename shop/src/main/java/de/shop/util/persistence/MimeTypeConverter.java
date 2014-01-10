package de.shop.util.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 */
@Converter(autoApply = true)
public class MimeTypeConverter implements AttributeConverter<MimeType, String> {
	@Override
	public String convertToDatabaseColumn(MimeType mimeType) {
		return mimeType.getExtension();
	}

	@Override
	public MimeType convertToEntityAttribute(String dbString) {
		return MimeType.buildFromExtension(dbString);
	}
}
