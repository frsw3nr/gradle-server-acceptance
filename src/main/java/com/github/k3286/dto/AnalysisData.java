package com.github.k3286.dto;

import java.math.BigDecimal;
import java.util.Date;

public class AnalysisData {

    /** 計上日 */
    private Date postingDate;

    /** 顧客名 */
    private String clientName;

    /** 売上高 */
    private BigDecimal salesAmt;

    /** 経費 */
    private BigDecimal costAmt;

    public AnalysisData() {
    }

    public AnalysisData(//
            Date postingDate //
            , String clientName //
            , BigDecimal salesAmt //
            , BigDecimal costAmt) {

        this.postingDate = postingDate;
        this.clientName = clientName;
        this.salesAmt = salesAmt;
        this.costAmt = costAmt;
    }

    /**
     * @return postingDate
     */
    public Date getPostingDate() {
        return postingDate;
    }

    /**
     * @param postingDate セットする postingDate
     */
    public void setPostingDate(Date postingDate) {
        this.postingDate = postingDate;
    }

    /**
     * @return clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientName セットする clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return salesAmt
     */
    public BigDecimal getSalesAmt() {
        return salesAmt;
    }

    /**
     * @param salesAmt セットする salesAmt
     */
    public void setSalesAmt(BigDecimal salesAmt) {
        this.salesAmt = salesAmt;
    }

    /**
     * @return costAmt
     */
    public BigDecimal getCostAmt() {
        return costAmt;
    }

    /**
     * @param costAmt セットする costAmt
     */
    public void setCostAmt(BigDecimal costAmt) {
        this.costAmt = costAmt;
    }

}
