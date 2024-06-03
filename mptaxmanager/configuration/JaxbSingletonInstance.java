package com.nike.mptaxmanager.configuration;

import com.nike.mptaxmanager.model.onesource.response.Outdata;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Slf4j
public final class JaxbSingletonInstance {
    private JaxbSingletonInstance() {
        // The body of private constructor
    }

    public static JAXBContext getInstance() {
        return SingletonHolder.instance;
    }

    // Inner static class that holds a reference to the singleton
    private static class SingletonHolder {
        private static JAXBContext instance;

        static {
            try {
                instance = JAXBContext.newInstance(Outdata.class);
            } catch (JAXBException e) {
                log.error("Error constructing JAXBContext " + e.getMessage());
            }
        }
    }
}


