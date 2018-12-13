package abc.integratedtest2;

import java.util.ArrayList;

/**
 * Created by HAN on 2017-11-15.
 */

public class BeforeOrder {
    private static ArrayList<ListItem> single_items; // 현재 선택되어있는 주문 선택 사항
    private static ArrayList<ListItem> set_items;
    private static ArrayList<ListItem> side_items;

    // 아이템 리스트
    public static ArrayList<ListItem> getSingle_items() {
        return single_items;
    }

    public static void setSingle_items(ArrayList<ListItem> single_items) {
        BeforeOrder.single_items = single_items;
    }

    public static void addSingle_items(String type, String name, String price, int quantity, int single_quantity, int set_quantity, int side_quantity) {
        single_items.add(new ListItem(type, name, price, quantity, single_quantity, set_quantity, side_quantity));
    }

    public static ArrayList<ListItem> getSet_items() {
        return set_items;
    }

    public static void setSet_items(ArrayList<ListItem> set_items) {
        BeforeOrder.set_items = set_items;
    }

    public static void addSet_items(String type, String name, String price, int quantity, int single_quantity, int set_quantity, int side_quantity) {
        set_items.add(new ListItem(type, name, price, quantity, single_quantity, set_quantity, side_quantity));
    }

    public static ArrayList<ListItem> getSide_items() {
        return side_items;
    }

    public static void setSide_items(ArrayList<ListItem> side_items) {
        BeforeOrder.side_items = side_items;
    }

    public static void addSide_items(String type, String name, String price, int quantity, int single_quantity, int set_quantity, int side_quantity) {
        side_items.add(new ListItem(type, name, price, quantity, single_quantity, set_quantity, side_quantity));
    }

    // 목록 초기화
    public static void reset() {
        single_items.clear();
        single_items.trimToSize();
        set_items.clear();
        single_items.trimToSize();
        side_items.clear();
        single_items.trimToSize();
    }
}
