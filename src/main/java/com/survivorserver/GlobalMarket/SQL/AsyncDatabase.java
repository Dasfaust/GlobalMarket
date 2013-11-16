package com.survivorserver.GlobalMarket.SQL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.Market;

public class AsyncDatabase {

	private Market market;
	private CopyOnWriteArrayList<QueuedStatement> queue;
	private boolean isProcessing = false;
	private int taskId = -1;
	private Database db;
	
	public AsyncDatabase(Market market) {
		this.market = market;
		queue = new CopyOnWriteArrayList<QueuedStatement>();
	}
	
	public void startTask() {
		if (db == null) {
			db = market.getConfigHandler().createConnection();
			db.connect();
		}
		taskId = new BukkitRunnable() {
			
			@Override
			public void run() {
				processQueue(false);
				if (market.isEnabled()) {
					startTask();
				}
			}
		}.runTaskLaterAsynchronously(market, taskId == -1 ? 0 : 1200).getTaskId();
	}
	
	public void processQueue(boolean debug) {
		debug = true;
		if (!db.isConnected()) {
			db.connect();
		}
		if (queue.size() > 0) {
			isProcessing = true;
			if (debug) {
				market.log.info("Processing database queue (size: " + queue.size() + ")");
			}
			Iterator<QueuedStatement> iterator = queue.iterator();
			List<QueuedStatement> processed = new ArrayList<QueuedStatement>();
			while(iterator.hasNext() && !market.haultSync()) {
				QueuedStatement statement = iterator.next();
				statement.buildStatement(db)
				.execute();
				processed.add(statement);
			}
			if (debug) {
				market.log.info("Queue finished. Processed " + processed.size() + " items");
			}
			queue.removeAll(processed);
			isProcessing = false;
		}
	}
	
	public void addStatement(QueuedStatement statement) {
		market.log.info("Adding item to queue: " + statement.toString());
		queue.add(statement);
	}
	
	public synchronized boolean isProcessing() {
		return isProcessing;
	}
	
	public synchronized int getTaskId() {
		return taskId;
	}
	
	public synchronized Database getDb() {
		return db;
	}
	
	public synchronized void close() {
		db.close();
	}
}
