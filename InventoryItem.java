package com.slginventory.model;

public class InventoryItem {
    private final Goods goods;
    private int quantity;

    public InventoryItem(Goods goods, int quantity) {
        this.goods = goods;
        this.quantity = quantity;
    }

    public Goods getGoods() { return goods; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}