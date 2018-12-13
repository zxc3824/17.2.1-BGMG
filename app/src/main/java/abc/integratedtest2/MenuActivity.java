package abc.integratedtest2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private ListView lv_single;
    private ListViewAdapter singleAdapter;
    private ListView lv_set;
    private ListViewAdapter setAdapter;
    private ListView lv_side;
    private ListViewAdapter sideAdapter;

    private LinearLayout layout;
    private TextView txtTotalPrice;
    private int total;
    private TextView txtSingleCount;
    private TextView txtSetCount;
    private TextView txtSideCount;

    private Button btn_confirm;

    protected static Activity menuActivity;

    private MyApplication myApp;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        myApp = (MyApplication)getApplication();

        myApp.setDialogContext(this);

        menuActivity = MenuActivity.this;

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/bdmj.ttf");
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        ((Button)findViewById(R.id.btn_confirm)).setTypeface(typeface);

        final TabHost tabHost;

        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        // 탭 추가
        TabHost.TabSpec tab1 = tabHost.newTabSpec("1").setContent(R.id.tab1).setIndicator("버거단품");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("2").setContent(R.id.tab2).setIndicator("버거세트");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("3").setContent(R.id.tab3).setIndicator("사이드메뉴");

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        // 탭 배경 설정
        for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#f1c84a"));
            ((TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title)).setTypeface(typeface);
            ((TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
        }
        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#e75037"));
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#f1c84a"));
                }
                tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#e75037"));
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

        // 초기화
        lv_single = (ListView)findViewById(R.id.lv_single);
        lv_set = (ListView)findViewById(R.id.lv_set);
        lv_side = (ListView)findViewById(R.id.lv_side);

        btn_confirm = (Button)findViewById(R.id.btn_confirm);

        total = 0;

        txtTotalPrice.setText(String.format("%s", preferences.getInt("beforeOrderPrice", 0)));
        txtSingleCount.setText(preferences.getString("beforeSingleCount", "0"));
        txtSetCount.setText(preferences.getString("beforeSetCount", "0"));
        txtSideCount.setText(preferences.getString("beforeSideCount", "0"));

        if(RECOActivity.getBeacon_num().equals("")) {
            informLayout = (LinearLayout)findViewById(R.id.informLayout);
            Button btn_confirm = (Button)findViewById(R.id.btn_confirm);

            informLayout.setVisibility(View.GONE);
            btn_confirm.setVisibility(View.GONE);
        }

        // 개수 변경 처리
        txtTotalPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putInt("beforeOrderPrice", Integer.parseInt(txtTotalPrice.getText().toString()));
                editor.apply();
            }
        });

        txtSingleCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("beforeSingleCount", txtSingleCount.getText().toString());
                editor.apply();
            }
        });

        txtSetCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("beforeSetCount", txtSetCount.getText().toString());
                editor.apply();
            }
        });

        txtSideCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("beforeSideCount", txtSideCount.getText().toString());
                editor.apply();
            }
        });

        ListTask task = new ListTask();
        task.execute();
    }

    public void onClick_confirm(View v) {
        if(!txtTotalPrice.getText().toString().equals("0")) {
            ArrayList<ListItem> items = new ArrayList<>();
            total = 0;
            ListItem item;
            for (int i = 0; i < BeforeOrder.getSingle_items().size(); i++) {
                item = BeforeOrder.getSingle_items().get(i);
                total += Integer.parseInt(item.getPriceStr()) * item.getQuantity();
                if (item.getQuantity() > 0) items.add(item);
            }
            for (int i = 0; i < BeforeOrder.getSet_items().size(); i++) {
                item = BeforeOrder.getSet_items().get(i);
                total += Integer.parseInt(item.getPriceStr()) * item.getQuantity();
                if (item.getQuantity() > 0) items.add(item);
            }
            for (int i = 0; i < BeforeOrder.getSide_items().size(); i++) {
                item = BeforeOrder.getSide_items().get(i);
                total += Integer.parseInt(item.getPriceStr()) * item.getQuantity();
                if (item.getQuantity() > 0) items.add(item);
            }

            // 선택한 아이템 목록 추가
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("selectedOrderPrice", total);
            editor.putString("selectedSingleCount", preferences.getString("beforeSingleCount", "0"));
            editor.putString("selectedSetCount", preferences.getString("beforeSetCount", "0"));
            editor.putString("selectedSideCount", preferences.getString("beforeSideCount", "0"));
            editor.apply(); // API 9 아래는 commit 사용

            Intent intent = new Intent(this, ConfirmActivity.class);
            intent.putExtra("items", items);
            intent.putExtra("isMain", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

            items.clear();
            items.trimToSize();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public class ListTask extends AsyncTask<Void, Void, Void> {

        private String sendMsg, receiveMsg;
        private JSONArray jsonArray;
        private ProgressBar pb_menu = (ProgressBar) findViewById(R.id.pb_menu);
        private String beacon_num;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("체크", "onPreExecute");

            // 로딩 시작
            btn_confirm.setClickable(false);
            pb_menu.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("체크", "doInBackground");

            // ★android.permission.INTERNET 퍼미션 없을 시 Exception
            if(BeforeOrder.getSingle_items() == null || BeforeOrder.getSingle_items().isEmpty()) {
                try {
                    URL url = new URL("http://123.214.204.211:8080/Capstone2/main/menulist.jsp");

                    beacon_num = RECOActivity.getBeacon_num();

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    url = null;

                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST"); // 전송 방식

                    // Request
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    sendMsg = "beacon_num=" + beacon_num; // 보낼 정보
                    osw.write(sendMsg); // 전송
                    osw.flush();

                    Log.d("체크", "확인 전");
                    // Request 이후 Response
                    if (conn.getResponseCode() == conn.HTTP_OK) { // 응답 코드가 200 인 경우
                        Log.d("체크", "HTTP_OK");
                        InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8"); // 인코딩은 선택
                        BufferedReader reader = new BufferedReader(isr);
                        StringBuffer buffer = new StringBuffer();
                        String str;
                        // Response 값 삽입
                        while ((str = reader.readLine()) != null) {
                            buffer.append(str);
                        }
                        receiveMsg = buffer.toString();
                        try {
                            jsonArray = new JSONArray(receiveMsg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("buffer : ", buffer.toString());

                        buffer = null;
                        isr = null;

                    } else {
                        Log.i("통신 결과", conn.getResponseCode() + "에러"); // 에러 코드 확인용
                    }

                    conn.disconnect();
                    osw = null;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                BeforeOrder.setSingle_items(new ArrayList<ListItem>());
                BeforeOrder.setSet_items(new ArrayList<ListItem>());
                BeforeOrder.setSide_items(new ArrayList<ListItem>());

                try {
                    JSONObject jsonObject;
                    Bitmap bitmap;
                    String defaultUrl = "http://123.214.204.211:8080/Capstone2/img/menu/";
                    URLConnection conn;
                    BufferedInputStream bis;
                    myApp.setSingleBitmaps(new ArrayList<Bitmap>());
                    myApp.setSetBitmaps(new ArrayList<Bitmap>());
                    myApp.setSideBitmaps(new ArrayList<Bitmap>());
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            conn = new URL(defaultUrl + jsonObject.getString("filename")).openConnection();
                            conn.connect();
                            bis = new BufferedInputStream(conn.getInputStream());
                            bitmap = BitmapFactory.decodeStream(bis);
                            bis.close();

                            switch (jsonObject.getString("type")) {
                                case "single":
                                    BeforeOrder.addSingle_items(jsonObject.getString("type"), jsonObject.getString("name"), jsonObject.getString("price"), 0, 0, 0, 0);
                                    myApp.addSingleBitsmaps(bitmap);
                                    break;
                                case "set":
                                    BeforeOrder.addSet_items(jsonObject.getString("type"), jsonObject.getString("name"), jsonObject.getString("price"), 0, 0, 0, 0);
                                    myApp.addSetBitsmaps(bitmap);
                                    break;
                                case "side":
                                    BeforeOrder.addSide_items(jsonObject.getString("type"), jsonObject.getString("name"), jsonObject.getString("price"), 0, 0, 0, 0);
                                    myApp.addSideBitsmaps(bitmap);
                                    break;
                            }
                        }
                    }
                    bitmap = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.d("체크", "onPostExecute");

            // 분류된 리스트 출력
            singleAdapter = new ListViewAdapter(MenuActivity.this, R.layout.menuitem, BeforeOrder.getSingle_items(), myApp.getSingleBitmaps(), myApp.getSetBitmaps(), myApp.getSideBitmaps(), txtTotalPrice, txtSingleCount, txtSetCount, txtSideCount, RECOActivity.getBeacon_num());
            lv_single.setAdapter(singleAdapter);
            setAdapter = new ListViewAdapter(MenuActivity.this, R.layout.menuitem, BeforeOrder.getSet_items(), myApp.getSingleBitmaps(), myApp.getSetBitmaps(), myApp.getSideBitmaps(), txtTotalPrice, txtSingleCount, txtSetCount, txtSideCount, RECOActivity.getBeacon_num());
            lv_set.setAdapter(setAdapter);
            sideAdapter = new ListViewAdapter(MenuActivity.this, R.layout.menuitem, BeforeOrder.getSide_items(), myApp.getSingleBitmaps(), myApp.getSetBitmaps(), myApp.getSideBitmaps(), txtTotalPrice, txtSingleCount, txtSetCount, txtSideCount, RECOActivity.getBeacon_num());
            lv_side.setAdapter(sideAdapter);

            // 로딩 완료
            pb_menu.setVisibility(View.GONE);
            btn_confirm.setClickable(true);
        }
    }
}
