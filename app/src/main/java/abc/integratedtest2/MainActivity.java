package abc.integratedtest2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HAN on 2017-10-25.
 */

public class MainActivity extends AppCompatActivity {
    private float downX, upX; //좌우로 슬라이드하는 제스처 인식을 위한 좌표 변수
    private int pos; //이 달의 메뉴에서 사진 번호 변수

    private SeekBar seekBar = null; //메뉴의 현재 번호 표시
    private ViewFlipper viewFlipper = null; //메뉴 이미지 모음
    private boolean bArtificial = false; //자동으로 넘어가는건지 수동으로 넘어가는건지 체크

    private Timer timer;
    static TextView txtState;
    private CheckTimerTask ctt;

    private MyApplication myApp;

    private static final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static boolean isStart = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myApp = (MyApplication)getApplication();

        myApp.setDialogContext(this);

        //글씨체 설정
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/bdmj.ttf");
        ((TextView)findViewById(R.id.txt_main1)).setTypeface(typeface);
        ((TextView)findViewById(R.id.txt_main2)).setTypeface(typeface);
        ((TextView)findViewById(R.id.txt_main3)).setTypeface(typeface);
        ((Button)findViewById(R.id.btnOrder)).setTypeface(typeface);
        ((Button)findViewById(R.id.btnResult)).setTypeface(typeface);
        ((TextView)findViewById(R.id.txtState)).setTypeface(typeface);

        //뷰 변수들 추출
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);

        //뷰플리퍼 인터벌 3초 간격, 애니메이션 설정
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setInAnimation(getApplicationContext(), R.anim.left_in);
        viewFlipper.setOutAnimation(getApplicationContext(), R.anim.left_out);

        //현재 메뉴 번호 초기화
        pos = -1;

        //seekBar 클릭으로 메뉴 변경 안되게 설정
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(pos);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //뷰플리퍼 수동으로 변경하게 설정
        viewFlipper.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    downX = event.getX();
                    viewFlipper.stopFlipping(); //누른 상태에서 자동으로 넘어가지 않게 설정
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    upX = event.getX();
                    //제스처 취하면 수동인걸 표시
                    bArtificial = true;

                    //다음 이미지 이동
                    if(upX < downX)
                    {
                        //메뉴 번호 + 1, 최대치 넘어가면 0부터 다시 시작
                        pos = (++pos + (seekBar.getMax() + 1)) % (seekBar.getMax() + 1);
                        viewFlipper.showNext();
                    }
                    //이전 이미지 이동
                    else if(upX > downX)
                    {
                        //메뉴 번호 - 1, 최소치 넘어가면 최대치부터 다시 시작
                        pos = (--pos + (seekBar.getMax() + 1)) % (seekBar.getMax() + 1);
                        //오른쪽으로 사라지는 이미지 설정
                        viewFlipper.setInAnimation(getApplicationContext(), R.anim.right_in);
                        viewFlipper.setOutAnimation(getApplicationContext(), R.anim.right_out);
                        viewFlipper.showPrevious();
                    }

                    //뷰플리퍼 다시 플리핑
                    viewFlipper.startFlipping();
                    //기존 애니메이션으로 복원
                    viewFlipper.setInAnimation(getApplicationContext(), R.anim.left_in);
                    viewFlipper.setOutAnimation(getApplicationContext(), R.anim.left_out);
                }
                return true;
            }
        });

        //뷰플리퍼 이미지 변경될 때 수동이면 그냥 변경, 자동이면 번호 바꿔서 변경
        viewFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bArtificial)
                {
                    bArtificial = false;
                }
                else
                {
                    pos = (++pos + (seekBar.getMax() + 1)) % (seekBar.getMax() + 1);
                }
                seekBar.setProgress(pos);
            }
        });

        // 현재 주문 개수 출력(다른 Activity 에서 돌아올 경우)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        txtState = findViewById(R.id.txtState);
        int oc = preferences.getInt("orderCount", 0);
        if(oc != 0) txtState.setText(getResources().getString(R.string.notify_exist_text, preferences.getInt("orderCount", 0)));

        //RECOActivity.setBeacon_num("1"); // 테스트용(Beacon_num 강제 고정)

        if(timer != null || ctt != null) { // ※cancel() 처리하지 않을 경우 해당 Activity 에 올 때마다 쓰레드 증가
            timer.cancel();
            ctt.cancel();
        }

        if(isStart) {
            ctt = new CheckTimerTask();
            timer = new Timer();
            timer.schedule(ctt, 0, 1000);
            isStart = false;
        }

        if(RECOActivity.getBeacon_num().equals("")) {
            Button btnOrder = (Button)findViewById(R.id.btnOrder);
            Button btnResult = (Button)findViewById(R.id.btnResult);

            btnOrder.setText("메뉴보기 >");
            btnResult.setOnClickListener(null);
            btnResult.setClickable(false);
            btnResult.setBackgroundColor(Color.parseColor("#999999"));

            LinearLayout ll_result = (LinearLayout)findViewById(R.id.ll_result);
            ll_result.setClickable(false);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        viewFlipper.startFlipping(); //정지 상태에서 돌아오면 플리핑 시작
    }

    @Override
    public void onPause()
    {
        super.onPause();
        viewFlipper.stopFlipping(); //정지 되면 플리핑 멈춤
    }

    // OrderActivity로 이동
    public void startOrder(View v)
    {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // ※해당 flag 미설정시 Activity 다중 실행(성능 하락 및 메모리 증가)
        startActivity(intent);
        finish();
    }

    // ResultActivity로 이동
    public void startResult(View v)
    {
        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("isMain", true);
        startActivity(intent);
        finish();
    }

    // 뒤로 버튼 처리
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else
        {
            backPressedTime = tempTime;
            Toast toast = Toast.makeText(getApplicationContext(), "\'뒤로\' 버튼을 다시 누르면 종료됩니다.\n알림을 받지 못하게 됩니다.", Toast.LENGTH_SHORT);
            TextView v = (TextView)toast.getView().findViewById(android.R.id.message);
            if(v != null) v.setGravity(Gravity.CENTER);
            toast.show();
        }
    }

    // 제작중인 주문 개수 확인(CheckTimerTask, OrderCheckTask)
    public class CheckTimerTask extends TimerTask {
        OrderCheckTask occ;

        protected CheckTimerTask() {
            super();
        }

        @Override
        public void run() {
            occ = new OrderCheckTask();
            occ.execute();
        }
    }

    public class OrderCheckTask extends AsyncTask<Void, Void, Void> {
        private String sendMsg, receiveMsg;
        private JSONArray jsonArray;
        private JSONObject jsonObject;

        private String uuid;

        private SharedPreferences preferences;
        private SharedPreferences.Editor editor;

        public OrderCheckTask() {
            super();
            uuid = DeviceUuidFactory.getDeviceUuid().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {

            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = preferences.edit();

            try {
                URL url = new URL("http://123.214.204.211:8080/Capstone2/main/ordercheck.jsp");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST"); // 전송 방식

                // Request
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "uuid='" + uuid + "'";
                osw.write(sendMsg); // 전송
                osw.flush();

                // Request 이후 Response
                if (conn.getResponseCode() == conn.HTTP_OK) { // 응답 코드가 200 인 경우
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
                        jsonObject = jsonArray.getJSONObject(0);
                        editor.putInt("orderCount", jsonObject.getInt("count"));
                        editor.apply();

                        jsonArray = null;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    isr = null;

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러"); // 에러 코드 확인용

                    if(preferences.getInt("orderCount", 0) == 0) { // 연결 문제 시 이전 수량 유지(Wi-Fi 연결 문제 방지)
                        editor.putInt("beforeCount", preferences.getInt("orderCount", 0));
                        editor.apply();
                    }
                }

                conn.disconnect();
                url = null;
                osw = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            Log.d("주문 체크", String.format("%s", preferences.getInt("orderCount", 0)));
            if(preferences.getInt("orderCount", 0) == 0) MainActivity.txtState.setText(R.string.state_unordered);
            else MainActivity.txtState.setText(getResources().getString(R.string.notify_exist_text, preferences.getInt("orderCount", 0)));

            if(preferences.getInt("orderCount", 0) < preferences.getInt("beforeCount", 0)) {
                long[] pattern = new long[] {100, 3000, 1000}; // 진동 패턴(대기, 진동, 대기, 진동...)

                NotificationManager notificationManager= (NotificationManager)MainActivity.this.getSystemService(MainActivity.this.NOTIFICATION_SERVICE);
                Intent intent1 = new Intent(MainActivity.this.getApplicationContext(),MainActivity.class); //인텐트 생성.

                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                //intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//현재 액티비티를 최상으로 올린다.

                MyApplication myApp = (MyApplication)getApplication();

                PendingIntent pendingNotificationIntent = PendingIntent.getActivity( myApp.getDialogContext()/*MainActivity.this*/,0, intent1, PendingIntent.FLAG_ONE_SHOT);
                /*PendingIntent는 일회용 인텐트 같은 개념입니다.
                FLAG_UPDATE_CURRENT - > 만일 이미 생성된 PendingIntent가 존재 한다면, 해당 Intent의 내용을 변경함.
                FLAG_CANCEL_CURRENT - .이전에 생성한 PendingIntent를 취소하고 새롭게 하나 만든다.
                FLAG_NO_CREATE -> 현재 생성된 PendingIntent를 반환합니다.
                FLAG_ONE_SHOT - >이 플래그를 사용해 생성된 PendingIntent는 단 한번밖에 사용할 수 없습니다.*/
                builder.setSmallIcon(R.drawable.ico_main).setTicker("주문 완료").setWhen(System.currentTimeMillis())
                        .setNumber(1).setContentTitle("버거머거").setContentText("주문이 완료되었습니다.")
                        .setVibrate(pattern) // 진동
                        .setDefaults(Notification.DEFAULT_SOUND /*| Notification.DEFAULT_VIBRATE*/) // Default 설정(사운드, 진동, 빛)
                        .setContentIntent(pendingNotificationIntent)
                        .setAutoCancel(true) // 아이콘 클릭 시 Cancel
                        .setOngoing(false); // 알람 클릭, 슬라이드 시에도 사라지지 않음
                //해당 부분은 API 4.1버전부터 작동합니다.
                // setSmallIcon - > 작은 아이콘 이미지
                // setTicker - > 알람이 출력될 때 상단에 나오는 문구.
                // setWhen -> 알림 출력 시간.
                // setContentTitle-> 알림 제목
                // setConentText->푸쉬내용

                notificationManager.notify(1, builder.build()); // Notification send

                pattern = null;
                builder = null;
            }

            editor.putInt("beforeCount", preferences.getInt("orderCount", 0));
            editor.apply();
        }
    }
}
