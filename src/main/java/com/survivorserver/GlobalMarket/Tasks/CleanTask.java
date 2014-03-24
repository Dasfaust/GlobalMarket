package com.survivorserver.GlobalMarket.Tasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.InterfaceHandler;
import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Market;

public class CleanTask implements Runnable {
	
    Market market;
    InterfaceHandler handler;

    public CleanTask(Market market, InterfaceHandler handler) {
        this.market = market;
        this.handler = handler;
    }

    @Override
    public void run() {
        List<InterfaceViewer> toRemove = new ArrayList<InterfaceViewer>();
        for (InterfaceViewer viewer : handler.getAllViewers()) {
            Player player = market.getServer().getPlayer(viewer.getViewer());
            if (player != null && player.getOpenInventory() == null) {
                toRemove.add(viewer);
            }
        }
        for (InterfaceViewer viewer : toRemove) {
            handler.removeViewer(viewer);
        }
    }
}
