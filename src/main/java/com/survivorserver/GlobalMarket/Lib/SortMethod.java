package com.survivorserver.GlobalMarket.Lib;

public enum SortMethod {

	DEFAULT("newest"),
	PRICE_LOWEST("lowest price"),
	PRICE_HIGHEST("highest price"),
	AMOUNT_HIGHEST("highest amount");
	
	private String method;
	
	private SortMethod(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return method;
	}
}
