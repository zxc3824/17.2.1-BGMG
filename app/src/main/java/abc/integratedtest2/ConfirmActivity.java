package abc.integratedtest2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by HAN on 2017-10-23.
 */

public class ConfirmActivity extends AppCompatActivity{

    private static final int REQUEST_SEND_ORDER = 1;

    private ListView lv_confirm;
    private PreOrderAdapter preAdapter;

    private LinearLayout layout;
    private TextView txtTotalPrice;
    private TextView txtSingleCount;
    private TextView txtSetCount;
    private TextView txtSideCount;

    private static ArrayList<ListItem> items;

    protected static Activity confirmActivity;

    private MyApplication myApp;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        myApp = (MyApplication)getApplication();

        myApp.setDialogContext(this);

        confirmActivity = ConfirmActivity.this;

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/bdmj.ttf");
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        ((Button)findViewById(R.id.btn_order)).setTypeface(typeface);

        //탭 설정
        final TabHost tabhost;

        tabhost = (TabHost)findViewById(R.id.tabhost);
        tabhost.setup();

        //탭분할 추가
        TabHost.TabSpec spec1 = tabhost.newTabSpec("현재주문").setContent(R.id.tab1).setIndicator("현재 주문내역");
        TabHost.TabSpec spec2 = tabhost.newTabSpec("전체주문").setContent(R.id.tab2).setIndicator("전체 주문내역");

        tabhost.addTab(spec1);
        tabhost.addTab(spec2);

        // 탭 배경 설정
        for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
            tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#f1c84a"));
            ((TextView)tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title)).setTypeface(typeface);
            ((TextView)tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
        }
        tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#e75037"));
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
                    tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#f1c84a"));
                }
                tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#e75037"));
            }
        });

        //하단 레이아웃의 스타일 설정
        layout = (LinearLayout)findViewById(R.id.informLayout);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService (this.LAYOUT_INFLATER_SERVICE);
        LinearLayout informLayout = (LinearLayout)inflater.inflate (R.layout.layout_inform, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        informLayout.setLayoutParams(params);
        layout.addView(informLayout);
        txtTotalPrice = (TextView)findViewById(R.id.txtTotalPrice);
        txtSingleCount = (TextView)findViewById(R.id.txtSingleCount);
        txtSetCount = (TextView)findViewById(R.id.txtSetCount);
        txtSideCount = (TextView)findViewById(R.id.txtSideCount);

        ((TextView)findViewById(R.id.txt_inform)).setTypeface(typeface);
        txtTotalPrice.setTypeface(typeface);
        txtSingleCount.setTypeface(typeface);
        txtSetCount.setTypeface(typeface);
        txtSideCount.setTypeface(typeface);

        params = null;

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // 현 주문 내역
        txtTotalPrice.setText(String.format("%s", preferences.getInt("selectedOrderPrice", 0)));
        txtSingleCount.setText(preferences.getString("selectedSingleCount", "0"));
        txtSetCount.setText(preferences.getString("selectedSetCount", "0"));
        txtSideCount.setText(preferences.getString("selectedSideCount", "0"));

        if(items == null || items.size() == 0) items = new ArrayList<>();
        if(getIntent().getSerializableExtra("items") != null) items = (ArrayList<ListItem>)getIntent().getSerializableExtra("items");

        lv_confirm = (ListView)findViewById(R.id.lv_confirm);
        preAdapter = new PreOrderAdapter(this, R.layout.preorderitem, items);
        lv_confirm.setAdapter(preAdapter);

        // 총 주문 내역 List
        OrderTask orderTask = new OrderTask();
        orderTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();

        txtTotalPrice.setText(String.format("%s", preferences.getInt("selectedOrderPrice", 0)));
        txtSingleCount.setText(preferences.getString("selectedSingleCount", "0"));
        txtSetCount.setText(preferences.getString("selectedSetCount", "0"));
        txtSideCount.setText(preferences.getString("selectedSideCount", "0"));
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getBooleanExtra("isMain", true)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        /*Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();*/
    }

    public void onClick_order(View v) {
        if(preAdapter.getCount() > 0) {
            Intent intent = new Intent(this, RECOActivity.class);
            intent.putExtra("isLoading", false);
            startActivityForResult(intent, REQUEST_SEND_ORDER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SEND_ORDER && resultCode == Activity.RESULT_OK) {
            SendTask task = new SendTask();
            task.execute();
        } else if(requestCode == REQUEST_SEND_ORDER && resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(ConfirmActivity.this, "주문 가능 지역을 벗어났습니다.", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 주문 전송
    public class SendTask extends AsyncTask<Void, Void, Boolean> {

        private String sendMsg;

        private SharedPreferences preferences;
        private SharedPreferences.Editor editor;

        @Override
        protected Boolean doInBackground(Void... params) {

            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = preferences.edit();

            try {
                URL url = new URL("http://123.214.204.211:8080/Capstone2/main/order.jsp");

                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                String today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST"); // 전송 방식

                // Request
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                sendMsg += "&uuid=" + DeviceUuidFactory.getDeviceUuid().toString();
                sendMsg += "&date=" + today;
                ListItem item;
                for(int i = 0; i < ConfirmActivity.items.size(); i++) {
                    item = ConfirmActivity.items.get(i);
                    sendMsg += "&menu=" + item.getNameStr();
                    sendMsg += "&quantity=" + String.format("%s", item.getQuantity());
                    sendMsg += "&price=" + item.getPriceStr();
                    sendMsg += "&beacon_num=" + RECOActivity.getBeacon_num();
                }
                osw.write(sendMsg);
                osw.flush();

                // Request 이후 Response
                if(conn.getResponseCode() == conn.HTTP_OK) { // 응답 코드가 200 인 경우
                    Log.d("체크", "HTTP_OK");
                    return true;
                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러"); // 에러 코드 확인용
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            if(b) {
                reset(); // 전송 후 초기화

                preAdapter.notifyDataSetChanged();

                Toast.makeText(ConfirmActivity.this, "주문이 완료되었습니다.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ConfirmActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                MenuActivity menuActivity = (MenuActivity)MenuActivity.menuActivity;
                menuActivity.finish();
                finish();
            } else {
                Toast.makeText(ConfirmActivity.this, "주문 전송 실패", Toast.LENGTH_LONG).show();
            }
        }

        private void reset() {
            ConfirmActivity.items.clear();
            ConfirmActivity.items.trimToSize();
            myApp.reset();
            BeforeOrder.reset();
            editor.putInt("beforeOrderPrice", 0);
            editor.putString("beforeSingleCount", "0");
            editor.putString("beforeSetCount", "0");
            editor.putString("beforeSideCount", "0");
            editor.putInt("selectedOrderPrice", 0);
            editor.putString("selectedSingleCount", "0");
            editor.putString("selectedSetCount", "0");
            editor.putString("selectedSideCount", "0");
            editor.apply();
        }
    }

    // 총 주문 내역 출력
    public class OrderTask extends AsyncTask<Void, Void, JSONArray> {
        private String sendMsg, receiveMsg;
        private JSONArray jsonArray;

        private OrderListAdapter orderListAdapter;

        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                URL url = new URL("http://123.214.204.211:8080/Capstone2/main/orderlist.jsp");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST"); // 전송 방식

                // Request
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                sendMsg = "uuid='" + DeviceUuidFactory.getDeviceUuid().toString() + "'";
                Log.d("sendMsg 체크", sendMsg);
                osw.write(sendMsg);
                osw.flush();

                // Request 이후 Response
                if(conn.getResponseCode() == conn.HTTP_OK) { // 응답 코드가 200 인 경우
                    Log.d("체크", "HTTP_OK");
                    InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8"); // 인코딩은 선택
                    BufferedReader reader = new BufferedReader(isr);
                    StringBuffer buffer = new StringBuffer();
                    String str;
                    // Response 값 삽입
                    while((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                    Log.d("체크", buffer.toString());
                    try {
                        jsonArray = new JSONArray(receiveMsg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러"); // 에러 코드 확인용
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray s) {
            super.onPostExecute(s);

            if(s == null) return;

            ArrayList<OrderItem> orders = new ArrayList<>();

            try {
                JSONObject object;

                for(int i = 0; i < s.length(); i++) {
                    object = s.getJSONObject(i);

                    orders.add(new OrderItem(object.getString("date"), object.getString("total"), object.getString("state")));
                }

                ListView lv_orderlist = (ListView)findViewById(R.id.lv_orderlist);
                orderListAdapter = new OrderListAdapter(ConfirmActivity.this, R.layout.orderlistitem, orders);
                lv_orderlist.setAdapter(orderListAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
