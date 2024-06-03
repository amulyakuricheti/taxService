//package com.nike.invoiceshipmentwrkr.controller;
//
//import com.google.common.collect.ImmutableMap;
//import com.nike.camel.processor.DistributedTraceProcessor;
//import com.nike.invoiceshipmentwrkr.cache.InvoiceShipmentWrkrCache;
//import com.nike.pace.validator.common.annotation.JwtScope;
//import com.nike.phylon.jwt.auth.JWTAuthorizedFor;
//import com.nike.phylon.jwt.auth.JWTNotRequired;
//import com.nike.wingtips.Tracer;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.apache.camel.ProducerTemplate;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.MediaType;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import static com.nike.internal.util.StringUtils.isNotBlank;
//
///**
// * Example endpoint that allows you to post object to camel route.
// */
//@Slf4j
//@RestController
//@RequestMapping("/order_mgmt")
//public class InvoiceShipmentWrkrController {
//    public static final String TRACE_ID = "X-B3-TraceId";
//
//    @Autowired
//    private ProducerTemplate producerTemplate;
//
//    @Autowired
//    private InvoiceShipmentWrkrCache cache;
//
//    public InvoiceShipmentWrkrController(ProducerTemplate producerTemplate, InvoiceShipmentWrkrCache cache) {
//        this.producerTemplate = producerTemplate;
//        this.cache = cache;
//    }
//
//    @RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json"})
//    public Map<String, String> success() throws Exception {
//        return ImmutableMap.of("success", "true");
//    }
//
//    @RequestMapping(value = "/invoiceshipmentwrkr/v1",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_XML,
//            consumes = MediaType.APPLICATION_XML)
//    @JWTAuthorizedFor(apps = {"ordermgmt"})
//    @JwtScope("order_management:invoiceshipmentwrkr::create:")
//    public ResponseEntity processShipment(HttpServletRequest req, @RequestBody final String input, HttpServletResponse res) {
//        log.info("In InvoiceShipmentWrkr Controller for body={}", input);
//        val traceId = getTraceId();
//        Map<String, Object> headers = new HashMap<String, Object>();
//        headers.put(TRACE_ID, traceId);
//        ResponseEntity responseEntity = null;
//
//        try {
//            responseEntity = producerTemplate.requestBodyAndHeaders("direct:apiProcessXML", input, headers, ResponseEntity.class);
//        } catch (Exception e) {
//            log.info("Error occurred while processing the invoice, with body={}", input);
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        } finally {
//            DistributedTraceProcessor.completeSubSpans();
//        }
//        return responseEntity;
//    }
//
//    private String getTraceId() {
//        String traceId = Tracer.getInstance().getCurrentSpan() != null ? Tracer.getInstance().getCurrentSpan().getTraceId() : null;
//        if (isNotBlank(traceId)) {
//            log.info("FOUND existing traceId={}", traceId);
//            return traceId;
//        }
//        traceId = UUID.randomUUID().toString();
//        log.info("NOT FOUND traceId hence generated new one,traceId={}", traceId);
//        return traceId;
//    }
//    @RequestMapping(value = "/invoiceshipmentwrkr_clearCache/", method = RequestMethod.GET)
//    public ResponseEntity<String> clearCache() {
//        String message = StringUtils.EMPTY;
//        cache.invalidateAllShipNodes();
//
//        return new ResponseEntity<>(message, HttpStatus.OK);
//    }
//}
