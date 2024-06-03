package com.nike.mptaxmanager.controller;

import com.codahale.metrics.Counter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.mptaxmanager.cache.TaxManagerCache;
import com.nike.mptaxmanager.model.Order;
import com.nike.phylon.jwt.auth.JWTAuthorizedFor;
import com.nike.phylon.jwt.auth.JWTNotRequired;
import com.nike.signalfx.publisher.NikeMetricRegistry;
import com.nike.wingtips.Tracer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.nike.pace.validator.common.annotation.JwtScope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.nike.internal.util.StringUtils.isNotBlank;

/**
 * Example endpoint that allows you to post object to camel route.
 */
@Slf4j
@RestController
@RequestMapping("/order_mgmt")
public class TaxManagerController {
    public static final String TRACE_ID = "X-B3-TraceId";
    private NikeMetricRegistry nikeMetricRegistry;

    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private TaxManagerCache cache;

    private Counter postSuccessCount;
    private Counter postErrorCount;

    @Autowired
    public TaxManagerController(NikeMetricRegistry nikeMetricRegistry, ProducerTemplate producerTemplate, TaxManagerCache cache) {
        this.producerTemplate = producerTemplate;
        postSuccessCount = nikeMetricRegistry.getOrCreateCounter("mptaxmanager.post.successCount");
        postErrorCount = nikeMetricRegistry.getOrCreateCounter("mptaxmanager.post.errorCount");
        this.cache = cache;
    }

    @VisibleForTesting
    static Map<String, String> getMapOfReceivedHeaders(HttpServletRequest req) {
        Map<String, String> receivedHeaders = new HashMap<>();
        val incomingHeaders = req.getHeaderNames();
        while (incomingHeaders.hasMoreElements()) {
            val headerName = incomingHeaders.nextElement();
            val headerValue = req.getHeader(headerName);
            receivedHeaders.put(headerName, headerValue);
            log.info("incomingHeader: {}={}", headerName, headerValue);
        }
        return receivedHeaders;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json"})
    public Map<String, String> success() throws Exception {
        return ImmutableMap.of("success", "true");
    }

    @RequestMapping(value = "/tax_manager/v1",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XML,
            consumes = MediaType.APPLICATION_XML)
    @JWTAuthorizedFor(apps = {"ordermgmt", "mptaxmanager", "cereturn"})
    @JwtScope("order_management:tax_manager::create:")
    public ResponseEntity mpTaxManager(HttpServletRequest req, @Valid @RequestBody final Order input, HttpServletResponse res) {
        log.info("In Tax Manager Controller for orderNumber={}, with body={}", input.getOrderNo(), input);
        final long postCallStartTime = System.currentTimeMillis();
        val traceId = getTraceId();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("orderNumber", input.getOrderNo());
        headers.put(TRACE_ID, traceId);
        ResponseEntity responseEntity = null;

        try {
            responseEntity = producerTemplate.requestBodyAndHeaders("direct:RestInput", input, headers, ResponseEntity.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully calculated taxes for orderNumber={}, enterpriseCode={}, orderType={}",
                        input.getOrderNo(), input.getEnterpriseCode(), input.getOrderType());
                postSuccessCount.inc();
            } else {
                log.info("Error occurred while calculating taxes for orderNumber={}, enterpriseCode={}, orderType={}, statusCode={}",
                        input.getOrderNo(), input.getEnterpriseCode(), input.getOrderType(), responseEntity.getStatusCodeValue());
                postErrorCount.inc();
            }
        } catch (Exception e) {
            log.info("Error occurred while calculating taxes for orderNumber={}, with body={}", input.getOrderNo(), input);
            postErrorCount.inc();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("orderNumber={}, regionReference={}, response={}, timeMs={}", input.getOrderNo(), input.getEnterpriseCode(), responseEntity, (System.currentTimeMillis() - postCallStartTime));
         //   DistributedTraceProcessor.completeSubSpans();
        }
        return responseEntity;
    }

    @JWTNotRequired
    @RequestMapping(value = "/tax_manager_clearCache/{name}", method = RequestMethod.GET)
    public ResponseEntity<String> clearCache(@PathVariable(value = "name") final String name) {
        String message = null;
        switch (name) {
            case ("commodityCodes"):
                cache.invalidateAllCommodityCodes();
                message = "Cache Cleared for CommodityCodes";
                break;
            case ("shipNodes"):
                cache.invalidateAllShipNodes();
                message = "Cache Cleared for shipNodes";
                break;
            case ("all"):
                cache.invalidateAllCommodityCodes();
                cache.invalidateAllShipNodes();
                message = "Cache Cleared for CommodityCodes & shipNodes";
                break;
            default:
                message = "Found INVALID path=" + name + ", FAILED to clear cache.";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    private String getTraceId() {
        String traceId = Tracer.getInstance().getCurrentSpan() != null ? Tracer.getInstance().getCurrentSpan().getTraceId() : null;
        if (isNotBlank(traceId)) {
            log.info("FOUND existing traceId={}", traceId);
            return traceId;
        }
        traceId = UUID.randomUUID().toString();
        log.info("NOT FOUND traceId hence generated new one,traceId={}", traceId);
        return traceId;
    }
}

