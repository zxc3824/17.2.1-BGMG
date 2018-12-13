package abc.integratedtest2;

/**
 * Created by HAN on 2017-10-25.
 */

public class OrderItem {
    private String date;
    private String total;
    private String state;

    public OrderItem(String date, String total, String state) {
        this.date = date;
        this.total = total;
        this.state = state;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
