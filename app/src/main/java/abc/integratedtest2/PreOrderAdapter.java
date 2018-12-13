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
 * Created by HAN on 2017-10-25.
 */

public class PreOrderAdapter extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> items;

    public PreOrderAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<ListItem> objects) {
        super(context, resource, objects);
        items = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Context context = parent.getContext();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.preorderitem, parent, false);

            Typeface typeface = Typeface.createFromAsset(convertView.getContext().getAssets(), "Fonts/bdmj.ttf");

            ((TextView)convertView.findViewById(R.id.tv_preName)).setTypeface(typeface);
            ((TextView)convertView.findViewById(R.id.tv_preQuantity)).setTypeface(typeface);
            ((TextView)convertView.findViewById(R.id.tv_prePrice)).setTypeface(typeface);
        }

        // 그냥 넣어본 그라데이션
        float[] hsv = {360f / getCount() * position + 1, 0.15f, 1.6f};
        convertView.setBackgroundColor(Color.HSVToColor(hsv));

        TextView tv_preName = convertView.findViewById(R.id.tv_preName);
        TextView tv_preQuantity = convertView.findViewById(R.id.tv_preQuantity);
        TextView tv_prePrice = convertView.findViewById(R.id.tv_prePrice);

        tv_preName.setText(items.get(position).getNameStr());
        tv_preQuantity.setText(String.format("%s", items.get(position).getQuantity()));
        tv_prePrice.setText(items.get(position).getPriceStr());

        return convertView;
    }
}
