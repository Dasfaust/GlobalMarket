package com.survivorserver.GlobalMarket.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.Market;

public class AsyncDatabase {

	private Market market;
	private List<QueuedStatement> queue;
	private AtomicBoolean isProcessing;
	private int taskId = -1;
	private Database db;
	
	public AsyncDatabase(Market market) {
		this.market = market;
		queue = new CopyOnWriteArrayList<QueuedStatement>();
		isProcessing = new AtomicBoolean();
		isProcessing.set(false);
	}
	
	public void startTask() {
		if (db == null) {
			db = market.getConfigHandler().createConnection();
			db.connect();
		}
		taskId = new BukkitRunnable() {
			
			@Override
			public void run() {
				processQueue(true);
				if (market.isEnabled()) {
					startTask();
				}
			}
		}.runTaskLaterAsynchronously(market, taskId == -1 ? 0 : 1200).getTaskId();
	}
	
	public void processQueue(boolean debug) {
		if (debug) {
			market.log.info("#### DB Queue started ####");
		}
		long started = System.currentTimeMillis();
		if (!db.isConnected()) {
			if (debug) {
				market.log.info("DB has disconnected. Reconnecting...");
			}
			db.connect();
		}
		try {
			if (queue.size() > 0) {
				isProcessing.set(true);
				if (debug) {
					market.log.info("Processing database queue (size: " + queue.size() + ")");
				}
				List<QueuedStatement> processed = new ArrayList<QueuedStatement>();
				for (QueuedStatement statement : queue) {
					if (market.haultSync()) {
						break;
					}
					statement.buildStatement(db)
					.execute();
					processed.add(statement);
				}
				if (debug) {
					market.log.info("Queue done. Processed " + processed.size() + " items");
				}
				queue.removeAll(processed);
				isProcessing.set(false);
			}
		} catch(Exception e) {
			isProcessing.set(false);
			market.log.severe("Error while processing DB queue:");
			e.printStackTrace();
		}
		if (debug) {
			market.log.info("#### DB Queue finished (took " + ((System.currentTimeMillis() - started) / 1000) + " sec) ####");
		}
	}
	
	public synchronized void addStatement(QueuedStatement statement) {
		queue.add(statement);
		market.log.info("Added statement to DB queue. Current size: " + queue.size());
	}
	
	public synchronized boolean isProcessing() {
		return isProcessing.get();
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
