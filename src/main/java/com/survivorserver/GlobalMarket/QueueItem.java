package com.survivorserver.GlobalMarket;

public class QueueItem {
	
	public int id;
	public Long created;
	public Listing listing;
	public Mail mail;
	
	public QueueItem() {
	}
	
	public QueueItem(int id, long created, Mail mail) {
		this.id = id;
		this.created = created;
		this.mail = mail;
	}
	
	public QueueItem(int id, long created, Listing listing) {
		this.id = id;
		this.created = created;
		this.listing = listing;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public long getTime() {
		return created;
	}
	
	public void setTime(long created) {
		this.created = created;
	}
	
	public Listing getListing() {
		return listing;
	}
	
	public void setListing(Listing listing) {
		this.listing = listing;
	}
	
	public Mail getMail() {
		return mail;
	}
	
	public void setMail(Mail mail) {
		this.mail = mail;
	}
}
