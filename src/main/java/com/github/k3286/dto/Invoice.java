package com.github.k3286.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 請求クラス
 * @author HINA
 */
public class Invoice {

    /** 請求日 */
    private Date invoiceDate;

    /** 請求No. */
    private String invoiceNo;

    /** 顧客住所 */
    private String clientAddress;

    /** 顧客郵便番号 */
    private String clientPostCode;

    /** 顧客名 */
    private String clientName;

    /** 営業担当者 */
    private String salesRep;

    /** 立替金 */
    private BigDecimal advancePaid;

    /** 請求額（税込）*/
    private BigDecimal invoiceAmtTaxin;

    /** 税額 */
    private BigDecimal taxAmt;

    /** 備考 */
    private String note;

    /** 明細 */
    private List<InvoiceDetail> details = new ArrayList<InvoiceDetail>();

    /**
     * @return invoiceDateを取得する
     */
    public Date getInvoiceDate() {
        return invoiceDate;
    }

    /**
     * @param invoiceDateを設定する
     */
    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /**
     * @return invoiceNoを取得する
     */
    public String getInvoiceNo() {
        return invoiceNo;
    }

    /**
     * @param invoiceNoを設定する
     */
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    /**
     * @return clientNameを取得する
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientNameを設定する
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return salesRepを取得する
     */
    public String getSalesRep() {
        return salesRep;
    }

    /**
     * @param salesRepを設定する
     */
    public void setSalesRep(String salesRep) {
        this.salesRep = salesRep;
    }

    /**
     * @return advancePaidを取得する
     */
    public BigDecimal getAdvancePaid() {
        return advancePaid;
    }

    /**
     * @param advancePaidを設定する
     */
    public void setAdvancePaid(BigDecimal advancePaid) {
        this.advancePaid = advancePaid;
    }

    /**
     * @return invoiceAmtTaxinを取得する
     */
    public BigDecimal getInvoiceAmtTaxin() {
        return invoiceAmtTaxin;
    }

    /**
     * @param invoiceAmtTaxinを設定する
     */
    public void setInvoiceAmtTaxin(BigDecimal invoiceAmtTaxin) {
        this.invoiceAmtTaxin = invoiceAmtTaxin;
    }

    /**
     * @return taxAmtを取得する
     */
    public BigDecimal getTaxAmt() {
        return taxAmt;
    }

    /**
     * @param taxAmtを設定する
     */
    public void setTaxAmt(BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }

    /**
     * @return detailsを取得する
     */
    public List<InvoiceDetail> getDetails() {
        return details;
    }

    /**
     * @param detailsを設定する
     */
    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }

    /**
     * @return clientAddressを取得する
     */
    public String getClientAddress() {
        return clientAddress;
    }

    /**
     * @param clientAddressを設定する
     */
    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * @return clientPostCodeを取得する
     */
    public String getClientPostCode() {
        return clientPostCode;
    }

    /**
     * @param clientPostCodeを設定する
     */
    public void setClientPostCode(String clientPostCode) {
        this.clientPostCode = clientPostCode;
    }

    /**
     * @return noteを取得する
     */
    public String getNote() {
        return note;
    }

    /**
     * @param noteを設定する
     */
    public void setNote(String note) {
        this.note = note;
    }

}
