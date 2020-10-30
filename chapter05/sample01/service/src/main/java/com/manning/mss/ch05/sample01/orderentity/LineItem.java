package com.manning.mss.ch05.sample01.orderentity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LineItem {
	@JsonAlias("code")
    @JsonProperty("itemCode")
    private String itemCode;

	@JsonAlias("qty")
    @JsonProperty("quantity")
    private int quantity;

    public String getItemCode() {

        return itemCode;
    }

    public void setItemCode(String itemCode) {

        this.itemCode = itemCode;
    }

    public int getQuantity() {

        return quantity;
    }

    public void setQuantity(int quantity) {

        this.quantity = quantity;
    }
}
