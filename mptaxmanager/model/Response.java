package com.nike.mptaxmanager.model;


import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "Response")
public class Response {
    @XmlAttribute(name = "UpdateTax")
    private String draftOrderFlag;
    @XmlElement(name = "Order")
    private Order order;

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Order {

        @XmlAttribute(name = "OrderHeaderKey", required = true)
        private String orderHeaderKey;

        @XmlElement(name = "OrderLines")
        private OrderLines orderLines;
        @XmlElement(name = "HeaderTaxes")
        private HeaderTaxes headerTaxes;
        @XmlElement(name = "InvoiceTaxes")
        private InvoiceTaxes invoiceTaxes;

        @Getter
        @Setter
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class OrderLines {

            @XmlElement(name = "OrderLine")
            private List<OrderLines> orderLines;

            @Getter
            @Setter
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class OrderLine {

                @XmlAttribute(name = "OrderLineKey", required = true)
                private String orderLineKey;
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class LineTaxess {

                    @XmlElement(name = "LineTax")
                    private List<LineTax> lineTaxes;

                    @Getter
                    @Setter
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class LineTax {

                        @XmlAttribute(name = "ChargeCategory", required = true)
                        private String chargeCategory;
                        @XmlAttribute(name = "ChargeName", required = true)
                        private String chargeName;
                        @XmlAttribute(name = "Reference1", required = true)
                        private String reference1;
                        @XmlAttribute(name = "Reference2")
                        private String reference2;
                        @XmlAttribute(name = "Reference3")
                        private String reference3;
                        @XmlAttribute(name = "Tax")
                        private double tax;
                        @XmlAttribute(name = "TaxName")
                        private String taxName;
                        @XmlAttribute(name = "TaxPercentage")
                        private double taxPercentage;


                    }
                }

            }
        }


        @Getter
        @Setter
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class HeaderTaxes {

            @XmlElement(name = "HeaderTax")
            private List<HeaderTax> headerTaxes;


            @Getter
            @Setter
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class HeaderTax {

                @XmlAttribute(name = "ChargeCategory", required = true)
                private String chargeCategory;
                @XmlAttribute(name = "ChargeName", required = true)
                private String chargeName;
                @XmlAttribute(name = "ChargeNameKey", required = true)
                private String chargeNameKey;
                @XmlAttribute(name = "Reference_1", required = true)
                private String reference1;
                @XmlAttribute(name = "Reference_2")
                private String reference2;
                @XmlAttribute(name = "Reference_3")
                private String reference3;
                @XmlAttribute(name = "Tax")
                private double tax;
                @XmlAttribute(name = "RemainingTax")
                private double remainingTax;
                @XmlAttribute(name = "TaxName")
                private String taxName;
                @XmlAttribute(name = "TaxPercentage")
                private double taxPercentage;
                @XmlAttribute(name = "TaxableFlag")
                private double taxableFlag;


            }
        }

        @Getter
        @Setter
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class InvoiceTaxes {

            @Getter
            @Setter
            @XmlElement(name = "InvoiceTax")
            private List<InvoiceTax> invoiceTaxes;


            @Getter
            @Setter
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class InvoiceTax {

                @XmlAttribute(name = "OrderLineKey", required = true)
                private String orderLineKey;
                @XmlAttribute(name = "ActualTax")
                private double actualTax;
                @XmlAttribute(name = "TaxPercentage")
                private double taxPercentage;

            }
        }

    }
}



