package com.survivorserver.GlobalMarket.SQL;

public enum StorageMethod {
	
	MYSQL("mysql"), SQLITE("sqlite");

	private String method;
	
	private StorageMethod(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return method;
	}
}
