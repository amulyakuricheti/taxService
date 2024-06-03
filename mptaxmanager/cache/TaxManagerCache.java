package com.nike.mptaxmanager.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.model.ShipNode;
import com.nike.mptaxmanager.model.taxClassificationEngine.TCCResponse.ClassificationCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaxManagerCache {

    static final int CACHE_SIZE = 20000;
    static final int CACHE_EXPIRE_IN_HOURS = 6;
    private final Cache<String, String> itemProxycache;
    private final Cache<String, ShipNode> shipNodecache;
    private final Cache<String, ClassificationCode> tccCache;


    public TaxManagerCache() {
        this.itemProxycache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_IN_HOURS, TimeUnit.HOURS)
                .build();
        this.shipNodecache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_IN_HOURS, TimeUnit.HOURS)
                .build();
        this.tccCache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_IN_HOURS, TimeUnit.HOURS)
                .build();
    }

    public String findCommodityCodeByGtin(final String upcCode) {
        log.info("cache : findCommodityCodeByGtin for upcCode={}", upcCode);
        String commodityCode = itemProxycache.getIfPresent(upcCode);
        if (StringUtils.isNotBlank(commodityCode)) {
            log.info("Loaded the commodityCode={} from cache for upcCode={}", commodityCode, upcCode);
            return commodityCode;
        }
        return commodityCode;
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

    public ClassificationCode findClassificationCodeByUPCCode(final String upcCode) {
        log.info("Fetching ClassificationCode from cache for UPCCode={}", upcCode);
        ClassificationCode classificationCode = tccCache.getIfPresent(upcCode);
        if (classificationCode != null) {
            log.info("Successfully fetched ClassificationCode={} by UPCCode={}", classificationCode, upcCode);
            return classificationCode;
        }
        return null;
    }

    public void putCommodityCodeByGtin(final String upcCode, final String commodityCodeIn) {
        String commodityCode = itemProxycache.getIfPresent(upcCode);
        if (StringUtils.isBlank(commodityCode)) {
            log.info("Put the commodityCode={} to cache for upcCode={}", commodityCodeIn, upcCode);
            itemProxycache.put(upcCode, commodityCodeIn);
        }
    }

    public void putShipNodeByKey(final String shipNodeKey, final ShipNode shipNodeIn) {
        ShipNode shipNode = shipNodecache.getIfPresent(shipNodeKey);
        if (shipNode == null) {
            log.info("Put the shipNodeId={} shipNodeAddress={} to cache for shipNode={}", shipNodeIn.getId(), shipNodeIn.getAddress(), shipNodeKey);
            shipNodecache.put(shipNodeKey, shipNodeIn);
        }
    }

    public void putClassificationCodeByUPCCode(final String upcCode, final ClassificationCode incomingTCC) {
        log.info("Caching ClassificationCode={} for UPCCode={}", incomingTCC, upcCode);

        ClassificationCode classificationCode = tccCache.getIfPresent(upcCode);
        if (classificationCode == null) {
            tccCache.put(upcCode, incomingTCC);
        }
    }

    public void invalidateAllShipNodes() {
        log.info("Deleting all shipNodes from cache");
        shipNodecache.invalidateAll();
    }

    public void invalidateAllCommodityCodes() {
        log.info("Deleting all ItemProxy commodityCodes from cache");
        itemProxycache.invalidateAll();
    }

    public void invalidateAllClassificationCodes() {
        log.info("Deleting all TCC classificationCodes from cache");
        tccCache.invalidateAll();
    }

}
