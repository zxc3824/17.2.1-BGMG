package abc.integratedtest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HAN on 2017-10-22.
 */

public class ListViewAdapter extends ArrayAdapter<ListItem>{

    private TextView totalView;
    private TextView singleCount;
    private TextView setCount;
    private TextView sideCount;
    private String beacon_num;
    private ArrayList<Bitmap> singleBitmaps;
    private ArrayList<Bitmap> setBitmaps;
    private ArrayList<Bitmap> sideBitmaps;

    public ListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<ListItem> objects, ArrayList<Bitmap> singleBitmaps, ArrayList<Bitmap> setBitmaps, ArrayList<Bitmap> sideBitmaps, TextView totalView, TextView singleCount, TextView setCount, TextView sideCount, String beacon_num) {
        super(context, resource, objects);

        this.totalView = totalView;
        this.singleCount = singleCount;
        this.setCount = setCount;
        this.sideCount = sideCount;
        this.beacon_num = beacon_num;
        this.singleBitmaps = singleBitmaps;
        this.setBitmaps = setBitmaps;
        this.sideBitmaps = sideBitmaps;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) { // ※parent 는 넣어질 ListView
        final ViewHolder holder;
        final Context context = parent.getContext();

        if(convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.menuitem, parent, false);

            holder.iv_icon = convertView.findViewById(R.id.iv_icon);
            holder.tv_name = convertView.findViewById(R.id.tv_name);
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            holder.tv_quantity = convertView.findViewById(R.id.tv_quantity);
            holder.btn_plus = convertView.findViewById(R.id.btn_plus);
            holder.btn_minus = convertView.findViewById(R.id.btn_minus);

            Typeface typeface = Typeface.createFromAsset(convertView.getContext().getAssets(), "Fonts/bdmj.ttf");

            holder.tv_name.setTypeface(typeface);
            holder.tv_price.setTypeface(typeface);
            holder.tv_quantity.setTypeface(typeface);

            // 정적인 부분 처리
            final String type = getItem(position).getTypeStr();;
            switch (type) {
                case "single":
                    //holder.iv_icon.setImageResource(R.drawable.single);
                    holder.iv_icon.setImageBitmap(singleBitmaps.get(position));
                    break;
                case "set":
                    //holder.iv_icon.setImageResource(R.drawable.set);
                    holder.iv_icon.setImageBitmap(setBitmaps.get(position));
                    break;
                case "side":
                    //holder.iv_icon.setImageResource(R.drawable.side);
                    holder.iv_icon.setImageBitmap(sideBitmaps.get(position));
                    break;
            }

            if (!beacon_num.equals("")) {
                holder.btn_plus.setOnClickListener(new View.OnClickListener() { // ★ViewHolder 에서 onClick 이벤트는 별도의 Button 인스턴스를 만들어 처리해야 함
                    @Override
                    public void onClick(View v) {
                        int quantity = Integer.parseInt(holder.tv_quantity.getText().toString()) + 1;
                        holder.tv_quantity.setText(String.format("%s", quantity));
                        getItem(position).setQuantity(quantity);

                        totalView.setText(String.format("%s", Integer.parseInt(totalView.getText().toString()) + Integer.parseInt(getItem(position).getPriceStr())));

                        switch (type) {
                            case "single":
                                int single_quantity = getItem(position).getSingle_quantity() + 1;
                                getItem(position).setSingle_quantity(single_quantity);
                                singleCount.setText(String.format("%s", Integer.parseInt(singleCount.getText().toString()) + 1));
                                break;
                            case "set":
                                int set_quantity = getItem(position).getSet_quantity() + 1;
                                getItem(position).setSet_quantity(set_quantity);
                                setCount.setText(String.format("%s", Integer.parseInt(setCount.getText().toString()) + 1));
                                break;
                            case "side":
                                int side_quantity = getItem(position).getSide_quantity() + 1;
                                getItem(position).setSide_quantity(side_quantity);
                                sideCount.setText(String.format("%s", Integer.parseInt(sideCount.getText().toString()) + 1));
                                break;
                        }
                    }
                });
                holder.btn_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int quantity = Integer.parseInt(holder.tv_quantity.getText().toString());
                        if (quantity != 0) { // 음수 방지
                            quantity -= 1;
                            holder.tv_quantity.setText(String.format("%s", quantity));
                            getItem(position).setQuantity(quantity);

                            totalView.setText(String.format("%s", Integer.parseInt(totalView.getText().toString()) - Integer.parseInt(getItem(position).getPriceStr())));

                            switch (type) {
                                case "single":
                                    int single_quantity = getItem(position).getSingle_quantity() - 1;
                                    getItem(position).setSingle_quantity(single_quantity);
                                    singleCount.setText(String.format("%s", Integer.parseInt(singleCount.getText().toString()) - 1));
                                    break;
                                case "set":
                                    int set_quantity = getItem(position).getSet_quantity() - 1;
                                    getItem(position).setSet_quantity(set_quantity);
                                    setCount.setText(String.format("%s", Integer.parseInt(setCount.getText().toString()) - 1));
                                    break;
                                case "side":
                                    int side_quantity = getItem(position).getSide_quantity() - 1;
                                    getItem(position).setSide_quantity(side_quantity);
                                    sideCount.setText(String.format("%s", Integer.parseInt(sideCount.getText().toString()) - 1));
                                    break;
                            }
                        }
                    }
                });
            } else { // 잡힌 비콘 없을 시
                holder.btn_minus.setVisibility(View.GONE);
                holder.btn_plus.setVisibility(View.GONE);
                holder.tv_quantity.setVisibility(View.GONE);
                holder.iv_icon.getLayoutParams().width = 350;
                holder.iv_icon.getLayoutParams().height = 350;
                holder.tv_name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.tv_price.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            }

            // 기본 틀 저장
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag(); // 이미 만들어진 Item 은 getTag() 통해 바로 호출
        }

        // 동적인 부분 처리
        holder.tv_name.setText(getItem(position).getNameStr());
        holder.tv_price.setText(getItem(position).getPriceStr());
        holder.tv_quantity.setText(String.format("%s",getItem(position).getQuantity()));

        return convertView;
    }

}
