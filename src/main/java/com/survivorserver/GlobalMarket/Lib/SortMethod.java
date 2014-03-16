package com.survivorserver.GlobalMarket.Lib;

public enum SortMethod {

	DEFAULT("default"),
	PRICE_LOWEST("price_lowest"),
	PRICE_HIGHEST("price_highest"),
	AMOUNT_HIGHEST("amount_highest"),
    LISTINGS_ONLY("listings_only"),
    MAIL_ONLY("mail_only");
	
	private String method;
	
	private SortMethod(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return method;
	}
}
