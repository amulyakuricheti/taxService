package com.nike.mptaxmanager.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Order")
public class Order {

    @NotNull
    @XmlElement(name = "PriceInfo", required = true)
    private PriceInfo priceInfo;

    @NotNull
    @XmlElement(name = "PersonInfoBillTo", required = true)
    private PersonInfoBillTo personInfoBillTo;

    @NotNull
    @XmlElement(name = "Extn", required = true)
    private Extn extn;

    @NotNull
    @XmlElement(name = "HeaderCharges", required = true)
    private HeaderCharges headerCharges;

    @NotNull
    @XmlElement(name = "OrderLines", required = true)
    private OrderLines orderLines;

    @NotNull
    @XmlElement(name = "References", required = true)
    private References references;

    @XmlElement(name = "CurrentShipmentShipFrom")
    private CurrentShipmentShipFrom currentShipmentShipFrom;

    @XmlElement(name = "OrderInvoiceList")
    private OrderInvoiceList orderInvoiceList;

    @NotEmpty
    @XmlAttribute(name = "OrderNo", required = true)
    private String orderNo;

    @NotEmpty
    @XmlAttribute(name = "OrderHeaderKey", required = true)
    private String orderHeaderKey;

    @NotEmpty
    @XmlAttribute(name = "EnterpriseCode", required = true)
    private String enterpriseCode;

    @NotEmpty
    @XmlAttribute(name = "Division", required = true)
    private String division;

    @NotEmpty
    @XmlAttribute(name = "OrderDate", required = true)
    private String orderDate;

    @NotEmpty
    @XmlAttribute(name = "OrderType", required = true)
    private String orderType;

    @XmlAttribute(name = "TaxExemptFlag")
    private String taxExemptFlag;

    @XmlAttribute(name = "IsNewOrder")
    private String isNewOrder;

    @XmlAttribute(name = "DraftOrderFlag")
    private String draftOrderFlag;

    @XmlAttribute(name = "CompanyCode")
    private String companyCode;

    @XmlAttribute(name = "DocumentType")
    private String documentType;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class CurrentShipmentShipFrom {

        @NotNull
        @XmlElement(name = "ShipNode", required = true)
        private CurrentShipmentShipFrom.ShipNode shipNode;

        @NotEmpty
        @XmlAttribute(name = "ShipmentKey", required = true)
        private String shipmentKey;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ShipNode {
            @XmlAttribute(name = "ShipNode")
            private String shipNode;

            @NotNull
            @XmlElement(name = "ShipNodePersonInfo", required = true)
            private PersonInfo shipNodePersonInfo;

            @NotEmpty
            @XmlAttribute(name = "ShipnodeKey", required = true)
            private String shipnodeKey;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PersonInfo {
                @XmlAttribute(name = "AddressID", required = false)
                private String addressID;

                @NotEmpty
                @XmlAttribute(name = "Country", required = true)
                private String country;

                @NotEmpty
                @XmlAttribute(name = "City", required = true)
                private String city;

                @NotEmpty
                @XmlAttribute(name = "State", required = true)
                private String state;

                @NotEmpty
                @XmlAttribute(name = "ZipCode", required = true)
                private String zipCode;

            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Extn {

        @XmlElement(name = "ExtnStoreList")
        private ExtnStoreList extnStoreList;

        @XmlAttribute(name = "ExtnComputeHeaderTax")
        private String extnComputeHeaderTax;

        @XmlAttribute(name = "ExtnLocale")
        private String extnLocale;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ExtnStoreList {

            @NotNull
            @XmlElement(name = "ExtnStore", required = true)
            private List<ExtnStore> extnStoreList;


            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ExtnStore {

                @NotEmpty
                @XmlAttribute(name = "ExtnTransEnd", required = true)
                private String extnTransEnd;

                @NotEmpty
                @XmlAttribute(name = "ExtnStoreID", required = true)
                private String extnStoreID;

            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class HeaderCharges {

        @NotNull
        @XmlElement(name = "HeaderCharge", required = true)
        private List<HeaderCharge> headerCharges;


        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class HeaderCharge {

            @XmlAttribute(name = "Reference", required = false)
            private String reference;
            @XmlAttribute(name = "IsDiscount", required = false)
            private String isDiscount;
            @XmlAttribute(name = "ChargeAmount", required = false)
            private String chargeAmount;
            @XmlAttribute(name = "ProductCode", required = false)
            private String productCode;
            @XmlAttribute(name = "ChargeCategory", required = false)
            private String chargeCategory;
        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OrderInvoiceList {

        @NotNull
        @XmlElement(name = "InvoiceDetail", required = true)
        private List<InvoiceDetail> invoiceDetails;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class InvoiceDetail {

            @NotNull
            @XmlElement(name = "InvoiceHeader", required = true)
            private InvoiceHeader invoiceHeader;

            @NotNull
            @XmlElement(name = "Shipments", required = true)
            private Order.OrderLines.OrderLine.Shipments shipments;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class InvoiceHeader {

                @NotNull
                @XmlElement(name = "HeaderCharges", required = true)
                private Order.HeaderCharges headerCharges;

                @NotNull
                @XmlElement(name = "Shipment", required = true)
                private Order.OrderLines.OrderLine.Shipments.Shipment shipment;

                @NotNull
                @XmlElement(name = "Extn", required = true)
                private Extn extn;

                @NotEmpty
                @XmlAttribute(name = "InvoiceType", required = true)
                private String invoiceType;
            }
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Extn {
            @XmlAttribute(name = "ExtnCustomerPONo")
            private String extnCustomerPONo;
        }

    }


    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OrderLines {

        @NotNull
        @XmlElement(name = "OrderLine", required = true)
        private List<Order.OrderLines.OrderLine> orderLine;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class OrderLine {

            @NotNull
            @XmlElement(name = "Item", required = true)
            private Item item;

            @NotNull
            @XmlElement(name = "Extn", required = true)
            private Extn lineExtn;

            @NotNull
            @XmlElement(name = "ItemDetails", required = true)
            private ItemDetails itemDetails;

            @NotNull
            @XmlElement(name = "LineOverallTotals", required = true)
            private LineOverallTotals lineOverallTotals;

            @NotNull
            @XmlElement(name = "LineTaxes", required = true)
            private LineTaxes lineTaxes;

            @NotNull
            @XmlElement(name = "LineCharges", required = true)
            private LineCharges lineCharges;

            @NotNull
            @XmlElement(name = "Shipments", required = true)
            private Shipments shipments;

            @NotNull
            @XmlElement(name = "PersonInfoShipTo", required = true)
            private CurrentShipmentShipFrom.ShipNode.PersonInfo personInfoShipTo;

            @NotNull
            @XmlElement(name = "PersonInfoMarkFor", required = true)
            private CurrentShipmentShipFrom.ShipNode.PersonInfo personInfoMarkFor;

            @NotEmpty
            @XmlAttribute(name = "OrderedQty", required = true)
            private String orderedQty;

            @NotEmpty
            @XmlAttribute(name = "OrderLineKey", required = true)
            private String orderLineKey;

            @NotEmpty
            @XmlAttribute(name = "PrimeLineNo", required = true)
            private String primeLineNo;

            @NotEmpty
            @XmlAttribute(name = "LineType", required = true)
            private String lineType;

            @NotEmpty
            @XmlAttribute(name = "KitCode", required = true)
            private String kitCode;

            @NotEmpty
            @XmlAttribute(name = "CarrierServiceCode", required = true)
            private String carrierServiceCode;

            @XmlAttribute(name = "FulfillmentType", required = true)
            private String fulfillmentType;

            @XmlAttribute(name = "BundleParentOrderLineKey")
            private String bundleParentOrderLineKey;
            @XmlAttribute(name = "MaxLineStatus")
            private String maxLineStatus;
            @XmlAttribute(name = "ShipToKey")
            private String shipToKey;
            @XmlAttribute(name = "UpdatedCommodityCode")
            private String updatedCommodityCode;
            @XmlAttribute(name = "ChainedFromOrderLineKey")
            private String chainedFromOrderLineKey;
            @XmlAttribute(name = "SendCommodityCode")
            private String sendCommodityCode;
            @XmlAttribute(name = "SendProductCode")
            private String sendProductCode;
            @XmlAttribute(name = "SendTaxProductCode")
            private String sendTaxProductCode;
            @XmlAttribute(name = "ExtnShipGroup")
            private String extnShipGroup;
            @XmlAttribute(name = "PackListType")
            private String packListType;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Extn {

                @XmlAttribute(name = "ExtnCommodityCode")
                private String extnCommodityCode;
                @XmlAttribute(name = "ExtnComputeTax")
                private String extnComputeTax;
                @XmlAttribute(name = "ExtnProductId")
                private String extnProductId;

                @NotEmpty
                @XmlAttribute(name = "ExtnShipGroup", required = true)
                private String extnShipGroup;
                @XmlAttribute(name = "ExtnCommerceItemID")
                private String extnCommerceItemID;

            }


            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Item {

                @NotEmpty
                @XmlAttribute(name = "ItemDesc", required = true)
                private String itemDesc;

                @NotEmpty
                @XmlAttribute(name = "TaxProductCode", required = true)
                private String taxProductCode;

                @NotEmpty
                @XmlAttribute(name = "UPCCode", required = true)
                private String upcCode;
                @XmlAttribute(name = "UnitOfMeasure")
                private String unitOfMeasure;

            }


            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ItemDetails {

                @NotNull
                @XmlElement(name = "ClassificationCodes", required = true)
                private ClassificationCodes classificationCodes;
                @XmlAttribute(name = "ItemID")
                private String itemID;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class ClassificationCodes {

                    @NotEmpty
                    @XmlAttribute(name = "TaxProductCode", required = true)
                    private String taxProductCode;

                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineCharges {

                @NotNull
                @XmlElement(name = "LineCharge", required = true)
                private List<LineCharges.LineCharge> lineCharges;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class LineCharge {

                    @NotEmpty
                    @XmlAttribute(name = "IsDiscount", required = true)
                    private String isDiscount;

                    @NotEmpty
                    @XmlAttribute(name = "RemainingChargePerUnit", required = true)
                    private String remainingChargePerUnit;

                    @NotEmpty
                    @XmlAttribute(name = "ChargeCategory", required = true)
                    private String chargeCategory;

                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineOverallTotals {

                @NotEmpty
                @XmlAttribute(name = "ExtendedPrice", required = true)
                private String extendedPrice;

                @NotEmpty
                @XmlAttribute(name = "PricingQty", required = true)
                private String pricingQty;

                @NotEmpty
                @XmlAttribute(name = "UnitPrice", required = true)
                private String unitPrice;

            }


            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineTaxes {

                @NotNull
                @XmlElement(name = "LineTax", required = true)
                private List<LineTaxes.LineTax> lineTaxes;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class LineTax {

                    @NotNull
                    @XmlElement(name = "Extn", required = true)
                    private LineTaxes.LineTax.Extn extn;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class Extn {

                        @XmlAttribute(name = "ExtnReference4")
                        private String extnReference4;

                    }

                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Shipments {

                @NotNull
                @XmlElement(name = "Shipment", required = true)
                private List<Shipment> shipments;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Shipment {

                    @XmlElement(name = "ShipmentLines", required = false)
                    private ShipmentLines shipmentLines;
                    @XmlElement(name = "ShipNode", required = false)
                    private Order.CurrentShipmentShipFrom.ShipNode shipNode;

                    @NotEmpty
                    @XmlAttribute(name = "ShipmentKey", required = true)
                    private String shipmentKey;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class ShipmentLines {

                        @XmlElement(name = "ShipmentLine", required = false)
                        private List<ShipmentLine> shipmentLines;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @XmlType(name = "")
                        public static class ShipmentLine {

                            @NotNull
                            @XmlElement(name = "OrderLine", required = true)
                            private com.nike.mptaxmanager.model.OrderLine orderLine;

                            @NotEmpty
                            @XmlAttribute(name = "Quantity", required = true)
                            private String quantity;

                            @NotEmpty
                            @XmlAttribute(name = "OrderLineKey", required = true)
                            private String orderLineKey;

                        }

                    }
                }

            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PersonInfoBillTo {

        @XmlAttribute(name = "AddressID")
        private String addressID;

        @NotEmpty
        @XmlAttribute(name = "Country", required = true)
        private String country;

        @NotEmpty
        @XmlAttribute(name = "State", required = true)
        private String state;

        @NotEmpty
        @XmlAttribute(name = "City", required = true)
        private String city;

        @NotEmpty
        @XmlAttribute(name = "ZipCode", required = true)
        private String zipCode;

    }


    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PriceInfo {

        @NotEmpty
        @XmlAttribute(name = "Currency", required = true)
        private String currency;

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class References {

        @NotNull
        @XmlElement(name = "Reference", required = true)
        private List<Order.References.Reference> references;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Reference {

            @NotEmpty
            @XmlAttribute(name = "Name", required = true)
            private String name;

            @NotEmpty
            @XmlAttribute(name = "Value", required = true)
            private String value;
        }

    }

}

