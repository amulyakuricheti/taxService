package com.nike.invoiceshipmentwrkr.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nike.invoiceshipmentwrkr.model.shipnode.ShipNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class InvoiceShipmentWrkrCache {

    static final int CACHE_SIZE = 20000;
    static final int CACHE_EXPIRE_IN_HOURS = 6;
    private final Cache<String, ShipNode> shipNodecache;

    public InvoiceShipmentWrkrCache() {
        this.shipNodecache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_IN_HOURS, TimeUnit.HOURS)
                .build();
    }

    public ShipNode findShipNodeByKey(final String shipNodeKey) {
        log.info("cache : findShipNodeByKey for  shipNode={}", shipNodeKey);
        ShipNode shipNode = shipNodecache.getIfPresent(shipNodeKey);
        if (shipNode != null) {
            log.info("Loaded the shipNodeId={} shipNodeAddress={} from cache for shipNode={}", shipNode.getId(), shipNode.getAddress(), shipNodeKey);
            return shipNode;
        }
        return null;
    }

    public void putShipNodeByKey(final String shipNodeKey, final ShipNode shipNodeIn) {
        ShipNode shipNode = shipNodecache.getIfPresent(shipNodeKey);
        if (shipNode == null) {
            log.info("Put the shipNodeId={} shipNodeAddress={} to cache for shipNode={}", shipNodeIn.getId(), shipNodeIn.getAddress(), shipNodeKey);
            shipNodecache.put(shipNodeKey, shipNodeIn);
        }
    }

    public void invalidateAllShipNodes() {
        log.info("Deleting all shipNodes from cache");
        shipNodecache.invalidateAll();
    }

}
