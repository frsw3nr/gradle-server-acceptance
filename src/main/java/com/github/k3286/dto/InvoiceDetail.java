package com.github.k3286.dto;

import java.math.BigDecimal;

/**
 * 請求明細クラス
 * @author HINA
 */
public class InvoiceDetail {

    /** 品名 */
    private String itemName;

    /** 単価 */
    private BigDecimal unitCost;

    /** 数量 */
    private Double quantity;

    /** 金額 */
    private BigDecimal amt;

    /**
     * @return itemNameを取得する
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param itemNameを設定する
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * @return unitCostを取得する
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    /**
     * @param unitCostを設定する
     */
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    /**
     * @return quantityを取得する
     */
    public Double getQuantity() {
        return quantity;
    }

    /**
     * @param quantityを設定する
     */
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    /**
     * @return amtを取得する
     */
    public BigDecimal getAmt() {
        return amt;
    }

    /**
     * @param amtを設定する
     */
    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

}
