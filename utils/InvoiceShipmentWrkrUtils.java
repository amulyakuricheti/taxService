package com.nike.invoiceshipmentwrkr.utils;

import com.nike.invoiceshipmentwrkr.model.shipment.References;
import com.nike.ordermgmt.model.orderdetails.Orderdetail;
import org.apache.commons.lang3.StringUtils;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public final class InvoiceShipmentWrkrUtils {
    private InvoiceShipmentWrkrUtils() { };

    public static final String START_TIME = "START_TIME";

    public static final int  MAX_ATTEMTS = 2;
    public static final int DELAY = 2000;

    public static String extractChannel(List<References.Reference> references) {
        String channel = StringUtils.EMPTY;
        for (References.Reference reference : references) {
            if (reference.getName().equalsIgnoreCase("Channel")
                    || reference.getName().equalsIgnoreCase("ChannelId")) {
                channel = reference.getValue();
                break;
            }
        }
        return channel;
    }

    public static String extractChannelFromOrderDetail(List<Orderdetail.Reference> references) {
        String channel = StringUtils.EMPTY;
        for (Orderdetail.Reference reference : references) {
            if (reference.getName().equalsIgnoreCase("Channel")
                    || reference.getName().equalsIgnoreCase("ChannelId")) {
                channel = reference.getValue();
                break;
            }
        }
        return channel;
    }

    public static String getFormatedDate(String format, int hour, String timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hour);
        String timestamp = formatter.format(cal.getTime());
        return timestamp;
    }
}
