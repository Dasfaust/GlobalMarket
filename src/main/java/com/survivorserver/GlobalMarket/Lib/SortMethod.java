package com.survivorserver.GlobalMarket.Lib;

public enum SortMethod {

	DEFAULT("latest"),
	PRICE_LOWEST("lowest_price"),
	PRICE_HIGHEST("highest_price"),
	AMOUNT_HIGHEST("highest_amount");
	
	private String method;
	
	private SortMethod(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return method;
	}
}
