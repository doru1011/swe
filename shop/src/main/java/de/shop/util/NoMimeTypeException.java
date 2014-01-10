package de.shop.util;

import javax.enterprise.inject.Vetoed;


/**
 * 
 */
@Vetoed
public class NoMimeTypeException extends AbstractShopException {
	private static final long serialVersionUID = -6174243392956089668L;
	
	private static final String MESSAGE_KEY = "file.noMimeType";

	public NoMimeTypeException() {
		super("Kein MIME-Type");
	}
	
	@Override
	public String getMessageKey() {
		return MESSAGE_KEY;
	}
}
