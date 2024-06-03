package com.nike.mptaxmanager.model.payload;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Order")
public class Order {

    @XmlElement(name = "PriceInfo", required = true)
    private Order.PriceInfo priceInfo;
    @XmlElement(name = "PersonInfoBillTo", required = true)
    private Order.PersonInfoBillTo personInfoBillTo;
    @XmlElement(name = "Extn", required = true)
    private Order.Extn extn;
    @XmlElement(name = "HeaderCharges", required = true)
    private Order.HeaderCharges headerCharges;
    @XmlElement(name = "References", required = true)
    private Order.References references;
    @XmlElement(name = "CurrentShipmentShipFrom", required = true)
    private Order.CurrentShipmentShipFrom currentShipmentShipFrom;
    @XmlElement(name = "OrderInvoiceList", required = true)
    private Order.OrderInvoiceList orderInvoiceList;
    @XmlElement(name = "OrderLines", required = true)
    private Order.OrderLines orderLines;
    @XmlAttribute(name = "OrderNo")
    private String orderNo;
    @XmlAttribute(name = "OrderHeaderKey")
    private String orderHeaderKey;
    @XmlAttribute(name = "EnterpriseCode")
    private String enterpriseCode;
    @XmlAttribute(name = "Division")
    private String division;
    @XmlAttribute(name = "OrderDate")
    private String orderDate;
    @XmlAttribute(name = "OrderType")
    private String orderType;
    @XmlAttribute(name = "TaxExemptFlag")
    private String taxExemptFlag;
    @XmlAttribute(name = "IsNewOrder")
    private String isNewOrder;
    @XmlAttribute(name = "DraftOrderFlag")
    private String draftOrderFlag;
    @XmlAttribute(name = "CompanyCode")
    private String companyCode;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class CurrentShipmentShipFrom {

        @XmlElement(name = "ShipNode", required = true)
        private Order.CurrentShipmentShipFrom.ShipNode shipNode;
        @XmlAttribute(name = "ShipmentKey")
        private String shipmentKey;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ShipNode {

            @XmlElement(name = "ShipNodePersonInfo", required = true)
            private Order.CurrentShipmentShipFrom.ShipNode.ShipNodePersonInfo shipNodePersonInfo;
            @XmlAttribute(name = "ShipnodeKey")
            private String shipnodeKey;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ShipNodePersonInfo {

                @XmlValue
                private String value;
                @XmlAttribute(name = "Country")
                private String country;
                @XmlAttribute(name = "City")
                private String city;
                @XmlAttribute(name = "State")
                private String state;
                @XmlAttribute(name = "ZipCode")
                private String zipCode;
            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Extn {

        @XmlElement(name = "ExtnStoreList", required = true)
        private Order.Extn.ExtnStoreList extnStoreList;
        @XmlAttribute(name = "ExtnComputeHeaderTax")
        private String extnComputeHeaderTax;
        @XmlAttribute(name = "ExtnLocale")
        private String extnLocale;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ExtnStoreList {

            @XmlElement(name = "ExtnStore", required = true)
            private List<Order.Extn.ExtnStoreList.ExtnStore> extnStore;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ExtnStore {

                @XmlValue
                private String value;
                @XmlAttribute(name = "ExtnTransEnd")
                private String extnTransEnd;
                @XmlAttribute(name = "ExtnStoreID")
                private String extnStoreID;

            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class HeaderCharges {

        @XmlElement(name = "HeaderCharge", required = true)
        private List<Order.HeaderCharges.HeaderCharge> headerCharge;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class HeaderCharge {

            @XmlValue
            private String value;
            @XmlAttribute(name = "Reference")
            private String reference;
            @XmlAttribute(name = "IsDiscount")
            private String isDiscount;
            @XmlAttribute(name = "ChargeAmount")
            private String chargeAmount;
            @XmlAttribute(name = "ProductCode")
            private String productCode;
        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OrderInvoiceList {

        @XmlElement(name = "InvoiceDetail", required = true)
        private List<Order.OrderInvoiceList.InvoiceDetail> invoiceDetail;
        @XmlElement(name = "OrderInvoice", required = true)
        private List<Order.OrderInvoiceList.OrderInvoice> orderInvoices;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class OrderInvoice {
            @XmlElement(name = "ShipFromForShippingCharge", required = true)
            private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipFromForShippingCharge;
            @XmlAttribute(name = "nShipNode")
            private String nShipNode;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class InvoiceDetail {

            @XmlElement(name = "InvoiceHeader", required = true)
            private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader invoiceHeader;
            @XmlElement(name = "Shipments", required = true)
            private Order.OrderInvoiceList.InvoiceDetail.Shipments shipments;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class InvoiceHeader {

                @XmlElement(name = "HeaderCharges", required = true)
                private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.HeaderCharges headerCharges;
                @XmlElement(name = "Shipment", required = true)
                private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment shipment;
                @XmlElement(name = "Extn")
                private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Extn extn;
                @XmlAttribute(name = "InvoiceType")
                private String invoiceType;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class HeaderCharges {

                    @XmlElement(name = "HeaderCharge", required = true)
                    private List<Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.HeaderCharges.HeaderCharge> headerCharge;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class HeaderCharge {

                        @XmlValue
                        private String value;
                        @XmlAttribute(name = "ChargeAmount")
                        private String chargeAmount;
                        @XmlAttribute(name = "IsDiscount")
                        private String isDiscount;
                        @XmlAttribute(name = "ChargeCategory")
                        private String chargeCategory;
                        @XmlAttribute(name = "ProductCode")
                        private String productCode;


                    }

                }

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Shipment {

                    @XmlElement(name = "ShipNode", required = true)
                    private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode shipNode;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class ShipNode {

                        @XmlElement(name = "ShipNodePersonInfo", required = true)
                        private Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipNodePersonInfo;
                        @XmlAttribute(name = "ShipnodeKey")
                        private String shipnodeKey;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @XmlType(name = "")
                        public static class ShipNodePersonInfo {

                            @XmlValue
                            private String value;
                            @XmlAttribute(name = "Country")
                            private String country;
                            @XmlAttribute(name = "City")
                            private String city;
                            @XmlAttribute(name = "State")
                            private String state;
                            @XmlAttribute(name = "ZipCode")
                            private String zipCode;
                        }

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
            public static class Shipments {

                @XmlElement(name = "Shipment", required = true)
                private List<Order.OrderInvoiceList.InvoiceDetail.Shipments.Shipment> shipment;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Shipment {

                    @XmlElement(name = "ShipNode", required = true)
                    private Order.OrderInvoiceList.InvoiceDetail.Shipments.Shipment.ShipNode shipNode;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class ShipNode {

                        @XmlElement(name = "ShipNodePersonInfo", required = true)
                        private Order.OrderInvoiceList.InvoiceDetail.Shipments.Shipment.ShipNode.ShipNodePersonInfo shipNodePersonInfo;
                        @XmlAttribute(name = "ShipNode")
                        private String shipNode;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @XmlType(name = "")
                        public static class ShipNodePersonInfo {

                            @XmlValue
                            private String value;
                            @XmlAttribute(name = "Country")
                            private String country;
                            @XmlAttribute(name = "City")
                            private String city;
                            @XmlAttribute(name = "State")
                            private String state;
                            @XmlAttribute(name = "ZipCode")
                            private String zipCode;

                        }

                    }

                }

            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OrderLines {

        @XmlElement(name = "OrderLine", required = true)
        private List<Order.OrderLines.OrderLine> orderLine;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class OrderLine {

            @XmlElement(name = "Item", required = true)
            private Order.OrderLines.OrderLine.Item item;
            @XmlElement(name = "Extn", required = true)
            private Order.OrderLines.OrderLine.Extn extn;
            @XmlElement(name = "ItemDetails", required = true)
            private Order.OrderLines.OrderLine.ItemDetails itemDetails;
            @XmlElement(name = "LineOverallTotals", required = true)
            private Order.OrderLines.OrderLine.LineOverallTotals lineOverallTotals;
            @XmlElement(name = "LineTaxes", required = true)
            private Order.OrderLines.OrderLine.LineTaxes lineTaxes;
            @XmlElement(name = "LineCharges", required = true)
            private Order.OrderLines.OrderLine.LineCharges lineCharges;
            @XmlElement(name = "PersonInfoShipTo", required = true)
            private Order.OrderLines.OrderLine.PersonInfoShipTo personInfoShipTo;
            @XmlElement(name = "PersonInfoMarkFor", required = true)
            private Order.OrderLines.OrderLine.PersonInfoMarkFor personInfoMarkFor;
            @XmlElement(name = "ShipFromAddress", required = true)
            private Order.OrderLines.OrderLine.ShipFromAddress shipFromAddress;
            @XmlAttribute(name = "nOrderLineKey")
            private String nOrderLineKey;
            @XmlAttribute(name = "nQty")
            private String nQty;
            @XmlAttribute(name = "OrderedQty")
            private String orderedQty;
            @XmlAttribute(name = "OrderLineKey")
            private String orderLineKey;
            @XmlAttribute(name = "PrimeLineNo")
            private Short primeLineNo;
            @XmlAttribute(name = "LineType")
            private String lineType;
            @XmlAttribute(name = "KitCode")
            private String kitCode;
            @XmlAttribute(name = "CarrierServiceCode")
            private String carrierServiceCode;
            @XmlAttribute(name = "FulfillmentType")
            private String fulfillmentType;
            @XmlAttribute(name = "BundleParentOrderLineKey")
            private String bundleParentOrderLineKey;
            @XmlAttribute(name = "MaxLineStatus")
            private String maxLineStatus;
            @XmlAttribute(name = "ShipToKey")
            private String shipToKey;
            @XmlAttribute(name = "UpdatedCommodityCode")
            private String updatedCommodityCode;
            @XmlAttribute(name = "CompanyCode")
            private String companyCode;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Extn {

                @XmlValue
                private String value;
                @XmlAttribute(name = "ExtnCommodityCode")
                private String extnCommodityCode;
                @XmlAttribute(name = "ExtnComputeTax")
                private String extnComputeTax;
                @XmlAttribute(name = "ExtnProductId")
                private String extnProductId;
                @XmlAttribute(name = "ExtnShipGroup")
                private String extnShipGroup;
                @XmlAttribute(name = "ExtnCommerceItemID")
                private String extnCommerceItemID;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Item {

                @XmlValue
                private String value;
                @XmlAttribute(name = "ItemDesc")
                private String itemDesc;
                @XmlAttribute(name = "TaxProductCode")
                private String taxProductCode;
                @XmlAttribute(name = "UPCCode")
                private String upcCode;
                @XmlAttribute(name = "UnitOfMeasure")
                private String unitOfMeasure;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ItemDetails {

                @XmlElement(name = "ClassificationCodes", required = true)
                private Order.OrderLines.OrderLine.ItemDetails.ClassificationCodes classificationCodes;
                @XmlAttribute(name = "ItemID")
                private String itemID;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class ClassificationCodes {

                    @XmlValue
                    private String value;
                    @XmlAttribute(name = "TaxProductCode")
                    private String taxProductCode;
                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineCharges {

                @XmlElement(name = "LineCharge", required = true)
                private List<Order.OrderLines.OrderLine.LineCharges.LineCharge> lineCharge;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class LineCharge {

                    @XmlValue
                    private String value;
                    @XmlAttribute(name = "IsDiscount")
                    private String isDiscount;
                    @XmlAttribute(name = "RemainingChargePerUnit")
                    private String remainingChargePerUnit;
                    @XmlAttribute(name = "ChargeCategory")
                    private String chargeCategory;
                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineOverallTotals {

                @XmlValue
                private String value;
                @XmlAttribute(name = "ExtendedPrice")
                private String extendedPrice;
                @XmlAttribute(name = "PricingQty")
                private String pricingQty;
                @XmlAttribute(name = "UnitPrice")
                private String unitPrice;

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineTaxes {

                @XmlElement(name = "LineTax", required = true)
                private List<Order.OrderLines.OrderLine.LineTaxes.LineTax> lineTax;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class LineTax {

                    @XmlElement(name = "Extn", required = true)
                    private Order.OrderLines.OrderLine.LineTaxes.LineTax.Extn extn;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class Extn {

                        @XmlValue
                        private String value;
                        @XmlAttribute(name = "ExtnReference4")
                        private String extnReference4;
                    }

                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PersonInfoMarkFor {

                @XmlValue
                private String value;
                @XmlAttribute(name = "AddressID")
                private String addressID;
                @XmlAttribute(name = "Country")
                private String country;
                @XmlAttribute(name = "City")
                private String city;
                @XmlAttribute(name = "State")
                private String state;
                @XmlAttribute(name = "ZipCode")
                private String zipCode;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PersonInfoShipTo {

                @XmlValue
                private String value;
                @XmlAttribute(name = "AddressID")
                private String addressID;
                @XmlAttribute(name = "Country")
                private String country;
                @XmlAttribute(name = "City")
                private String city;
                @XmlAttribute(name = "State")
                private String state;
                @XmlAttribute(name = "ZipCode")
                private String zipCode;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ShipFromAddress {

                @XmlValue
                private String value;
                @XmlAttribute(name = "ShipNode")
                private String shipNode;
                @XmlAttribute(name = "AddressLine1")
                private String addressLine1;
                @XmlAttribute(name = "AddressLine2")
                private String addressLine2;
                @XmlAttribute(name = "City")
                private String city;
                @XmlAttribute(name = "Company")
                private String company;
                @XmlAttribute(name = "Country")
                private String country;
                @XmlAttribute(name = "State")
                private String state;
                @XmlAttribute(name = "ZipCode")
                private String zipCode;
            }

        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PersonInfoBillTo {

        @XmlValue
        private String value;
        @XmlAttribute(name = "AddressID")
        private String addressID;
        @XmlAttribute(name = "Country")
        private String country;
        @XmlAttribute(name = "State")
        private String state;
        @XmlAttribute(name = "City")
        private String city;
        @XmlAttribute(name = "ZipCode")
        private String zipCode;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PriceInfo {

        @XmlValue
        private String value;
        @XmlAttribute(name = "Currency")
        private String currency;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class References {

        @XmlElement(name = "Reference", required = true)
        private List<Order.References.Reference> reference;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Reference {

            @XmlValue
            private String value;
            @XmlAttribute(name = "Name")
            private String name;
        }

    }

}

