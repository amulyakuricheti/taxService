package com.nike.mptaxmanager.route.processor;

import com.nike.cpe.vault.client.VaultClient;
import com.nike.cpe.vault.client.model.VaultResponse;
import com.nike.mptaxmanager.model.onesource.request.Indata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class PrepareOneSourceAuthProcessor implements Processor {

    private static final String CERBERUS_PATH = "shared/sabrix-users/taxcalc";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String XMLNS = "urn:nike:commerce:pricing:tax";

    private static final Map CREDENTIALS = new HashMap();

    @Autowired
    private VaultClient vaultClient;

    @Override
    public void process(Exchange exchange) {
        log.info("Control in PrepareOneSourceAuthProcessor");
        log.info("Control in PrepareOneSourceAuthProcessor data {}", exchange.getIn().getBody(String.class));

        String val = exchange.getIn().getBody(String.class);
        log.info("indata in PrepareOneSourceAuthProcessor indata val={}", val);

        Indata indata = exchange.getIn().getBody(Indata.class);
        log.info("indata in PrepareOneSourceAuthProcessor indata={}", indata);
        // added below 2 lines   to check if the framed xml doesn't have line, we  need recalculate Tax
        boolean isLineExist = validateLine(indata);
        log.info("is Valid =", isLineExist);
        exchange.setProperty("isLineExist", isLineExist);

        if (isLineExist) {
            log.info("setting up onesource creds");
            String userData = CREDENTIALS.get(USERNAME) != null ? CREDENTIALS.get(USERNAME).toString() : getCerberusProperties(USERNAME);
            String passData = CREDENTIALS.get(PASSWORD) != null ? CREDENTIALS.get(PASSWORD).toString() : getCerberusProperties(PASSWORD);
            indata.setUsername(userData);
            indata.setPassword(passData);
            indata.setXmlns(XMLNS);
            exchange.getIn().setBody(indata);
        }
    }

    private String getCerberusProperties(String property) {
        final VaultResponse response = vaultClient.read(CERBERUS_PATH);
        final Map<String, String> data = response.getData();
        String value = data.get(property);
        CREDENTIALS.put(property, value);
        return value;
    }

    private boolean validateLine(Indata indata) {
        boolean isValid = false;
        if (indata != null && indata.getInvoice() != null && indata.getInvoice().get(0) != null && indata.getInvoice().get(0).getLine() != null) {
            isValid = true;
        }
        return isValid;
    }

}

