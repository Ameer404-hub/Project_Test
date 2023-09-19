package com.example.project_test.Models;

public class ItemData {
    String Name, Qty, Desc, Status, PlacedBy, PurchasedBy;
    boolean visibility;

    public ItemData() {

    }

    public ItemData(String name, String qty, String desc, String status, String placedBy, String purchasedBy, boolean visibility) {
        Name = name;
        Qty = qty;
        Desc = desc;
        Status = status;
        PlacedBy = placedBy;
        PurchasedBy = purchasedBy;
        this.visibility = visibility;
    }

    public ItemData(String name, String qty, String desc, String status, String placedBy, String purchasedBy) {
        Name = name;
        Qty = qty;
        Desc = desc;
        Status = status;
        PlacedBy = placedBy;
        PurchasedBy = purchasedBy;
    }

    public ItemData(String name, String qty, String desc, String status, String purchasedBy) {
        Name = name;
        Qty = qty;
        Desc = desc;
        Status = status;
        PurchasedBy = purchasedBy;
    }

    public ItemData(String name, String desc) {
        Name = name;
        Desc = desc;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getQty() {
        return Qty;
    }

    public void setQty(String qty) {
        Qty = qty;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPlacedBy() {
        return PlacedBy;
    }

    public void setPlacedBy(String placedBy) {
        PlacedBy = placedBy;
    }

    public String getPurchasedBy() {
        return PurchasedBy;
    }

    public void setPurchasedBy(String purchasedBy) {
        PurchasedBy = purchasedBy;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
