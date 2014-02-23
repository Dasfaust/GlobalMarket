package com.survivorserver.GlobalMarket.SQL;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.Market;

public class AsyncDatabase {

	private Market market;
	private ConcurrentLinkedQueue<QueuedStatement> queue;
	private AtomicBoolean isProcessing;
	private AtomicBoolean cancel;
	private int taskId = -1;
	private Database db;
	
	public AsyncDatabase(Market market) {
		this.market = market;
		queue = new ConcurrentLinkedQueue<QueuedStatement>();
		isProcessing = new AtomicBoolean();
		isProcessing.set(false);
		cancel = new AtomicBoolean();
		cancel.set(false);
	}
	
	public void startTask() {
		if (db == null) {
			db = market.getConfigHandler().createConnection();
			db.connect();
		}
		taskId = new BukkitRunnable() {
			
			@Override
			public void run() {
				if (!queue.isEmpty() && !isProcessing.get()) {
					processQueue(false);
				}
				if (market.isEnabled() && !cancel.get()) {
					startTask();
				}
			}
			
		}.runTaskLaterAsynchronously(market, taskId == -1 ? 0 : 20).getTaskId();
	}
	
	public void processQueue(boolean debug) {
		if (debug) {
			market.log.info("#### DB Queue started ####");
		}
		long started = System.currentTimeMillis();
		if (!db.isConnected()) {
			market.log.severe("DB has disconnected. Reconnecting...");
			if (!db.connect()) {
				market.log.severe("Could not re-establish connection, queue stopped.");
				return;
			}
		}
		try {
			isProcessing.set(true);
			if (debug) {
				market.log.info("Processing database queue (size: " + queue.size() + ")");
			}
			int p = 0;
			while(!queue.isEmpty()) {
				QueuedStatement statement = queue.poll();
				statement.buildStatement(db).execute();
				p++;
			}
			if (debug) {
				market.log.info("Queue done. Processed " + p + " items");
			}
			isProcessing.set(false);
		} catch(Exception e) {
			isProcessing.set(false);
			market.log.severe("Error while processing DB queue:");
			e.printStackTrace();
		}
		if (debug) {
			market.log.info("#### DB Queue finished (took " + ((System.currentTimeMillis() - started) / 1000) + " sec) ####");
		}
	}
	
	public void addStatement(QueuedStatement statement) {
		queue.add(statement);
	}
	
	public boolean isProcessing() {
		return isProcessing.get();
	}
	
	public int getTaskId() {
		return taskId;
	}
	
	public Database getDb() {
		return db;
	}
	
	public void cancel() {
		cancel.set(true);
	}
	
	public synchronized void close() {
		db.close();
	}
}
