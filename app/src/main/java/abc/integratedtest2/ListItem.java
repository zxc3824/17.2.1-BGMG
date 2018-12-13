package abc.integratedtest2;

import java.io.Serializable;

/**
 * Created by HAN on 2017-10-22.
 */

public class ListItem implements Serializable { // ★Intent 로 ArrayList 넘겨야 하는 경우 Serializable 상속 필요
    private String typeStr;
    private String nameStr;
    private String priceStr;
    private int quantity;
    private int single_quantity;
    private int set_quantity;
    private int side_quantity;

    public ListItem(String typeStr, String nameStr, String priceStr, int quantity, int single_quantity, int set_quantity, int side_quantity) {
        this.typeStr = typeStr;
        this.nameStr = nameStr;
        this.priceStr = priceStr;
        this.quantity = quantity;
        this.single_quantity = single_quantity;
        this.set_quantity = set_quantity;
        this.side_quantity = side_quantity;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getNameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSingle_quantity() {
        return single_quantity;
    }

    public void setSingle_quantity(int single_quantity) {
        this.single_quantity = single_quantity;
    }

    public int getSet_quantity() {
        return set_quantity;
    }

    public void setSet_quantity(int set_quantity) {
        this.set_quantity = set_quantity;
    }

    public int getSide_quantity() {
        return side_quantity;
    }

    public void setSide_quantity(int side_quantity) {
        this.side_quantity = side_quantity;
    }
}
