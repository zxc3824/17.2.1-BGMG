package abc.integratedtest2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HAN on 2017-10-27.
 */

public class OrderListAdapter extends ArrayAdapter<OrderItem> {

    public OrderListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<OrderItem> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Context context = parent.getContext();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.orderlistitem, parent, false);

            Typeface typeface = Typeface.createFromAsset(convertView.getContext().getAssets(), "Fonts/bdmj.ttf");

            ((TextView)convertView.findViewById(R.id.tv_orderedDate)).setTypeface(typeface);
            ((TextView)convertView.findViewById(R.id.tv_orderTotal)).setTypeface(typeface);
        }

        TextView tv_date = convertView.findViewById(R.id.tv_orderedDate);
        TextView tv_total = convertView.findViewById(R.id.tv_orderTotal);

        tv_date.setText("일자 : " + getItem(position).getDate());
        tv_total.setText("총액 : ￦ " + getItem(position).getTotal());

        String state = getItem(position).getState();
        if(state.equals("FINISH")) {
            convertView.setBackgroundColor(Color.parseColor("#cccccc"));
        } else convertView.setBackgroundColor(Color.parseColor("#ffffff"));

        return convertView;
    }
}
