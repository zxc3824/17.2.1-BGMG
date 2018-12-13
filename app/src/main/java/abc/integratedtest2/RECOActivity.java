package abc.integratedtest2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Created by HAN on 2017-11-01.
 */

public class RECOActivity extends Activity implements RECOServiceConnectListener, RECORangingListener {

    protected RECOBeaconManager mRecoManager;
    protected ArrayList<RECOBeaconRegion> mRegions;

    private final long scanPeriod = 300;
    private static final long delayMillis = 1000;

    private ArrayList<Double> minAccuracies;
    private ArrayList<String> minBeacon_nums;

    private Runnable check;
    private Handler handle;
    private int times;
    private boolean isNext;
    
    private static String beacon_num;

    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco);

        RECOListTask recoListTask = new RECOListTask();
        try {
            mRegions = recoListTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(!mRegions.isEmpty()) {
            minAccuracies = new ArrayList<>();
            minBeacon_nums = new ArrayList<>();
            isNext = false;

            isLoading = getIntent().getBooleanExtra("isLoading", true);

            for (int i = 0; i < mRegions.size(); i++) {
                Log.d("체크", mRegions.get(i).getMajor() + ", " + mRegions.get(i).getUniqueIdentifier());
            }

            //RECORangingListener 설정(필수)
            mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, true);
            mRecoManager.setRangingListener(this);
            mRecoManager.setScanPeriod(scanPeriod); // 스캔 반복 시간

            if (isLoading && beacon_num == null) setBeacon_num(""); // beacon_num 값 초기화

            mRecoManager.bind(this);

            Log.d("RECOActivity", "Handler");
            handle = new Handler();

            times = (int) (delayMillis / scanPeriod);
            Log.d("RECOActivity", "times = " + String.format("%s", times));

            check = new Runnable() {
                @Override
                public void run() {
                    if (minAccuracies.size() > 0) {
                        // 각 Region 의 최소 range 값 중 최소 값 index 추출
                        double accuracy = 100;
                        int min = 0;
                        for (int i = 0; i < minAccuracies.size(); i++) {
                            if (minAccuracies.get(i) < accuracy) {
                                accuracy = minAccuracies.get(i);
                                min = i;
                            }
                        }

                        if (isLoading) { // 로딩 시 비콘 Region UID 값 저장
                            setBeacon_num(minBeacon_nums.get(min));
                        } else { // 주문 시 기존 비콘 값과 비교하여 맞으면 OK
                            Log.d("RECOActivity", "비콘 번호 : " + getBeacon_num());
                            if (getBeacon_num().equals(minBeacon_nums.get(min))) {
                                setResult(RESULT_OK);
                            } else setResult(RESULT_CANCELED);
                        }

                        if (!isNext) {
                            isNext = true;
                        }
                    }

                    //Log.d("RECOActivity", "비콘 번호 : " + getBeacon_num());
                    //if(!isLoading) setResult(RESULT_OK); // 테스트용(무조건 주문 가능)

                    times += 1;
                    if (times < 10) { // 체크 횟수
                        if (!isNext) handle.postDelayed(check, scanPeriod); // 체크 반복 시간
                        else nextActivity();
                    } else nextActivity();
                }
            };
            handle.postDelayed(check, delayMillis); // 체크 시작 시간(스캔 시간보다 늦게 설정 권장)
        }
    }

    public static String getBeacon_num() {
        return beacon_num;
    }

    public static void setBeacon_num(String beacon_num) {
        RECOActivity.beacon_num = beacon_num;
    }

    public void nextActivity() {
        Intent intent;
        if(isLoading) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() { // 로딩 중 뒤로가기 막기
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("최종 Beacon_num", getBeacon_num());
        this.stop(mRegions);
        this.unbind();
        if(!LoadingActivity.loadingActivity.isFinishing()) LoadingActivity.loadingActivity.finish();
    }

    private void unbind() {
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i("RECORangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(false);
        this.start(mRegions);
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());

        if(recoBeacons.size() != 0) {
            ArrayList<RECOBeacon> beacons = new ArrayList<>();
            beacons.addAll(recoBeacons);
            double minAccuracy = 100;
            for(int i = 0; i < beacons.size(); i++) {
                Log.d("recoBeacons", beacons.get(i).getProximityUuid());
                if(minAccuracy > beacons.get(i).getAccuracy()) {
                    minAccuracy = beacons.get(i).getAccuracy();
                }
                Log.d("recoBeacons", beacons.get(i).getMajor() + "번 노드 거리 = " + String.format("%s" ,beacons.get(i).getAccuracy()));
            }

            beacons.clear();
            beacons.trimToSize();

            minAccuracies.add(minAccuracy);
            minBeacon_nums.add(recoRegion.getUniqueIdentifier());

            Log.d("체크 Beacon_num", getBeacon_num());
        }
        //Write the code when the beacons in the region is received
    }

    protected void start(ArrayList<RECOBeaconRegion> regions) {

        /**
         * There is a known android bug that some android devices scan BLE devices only once. (link: http://code.google.com/p/android/issues/detail?id=65863)
         * To resolve the bug in our SDK, you can use setDiscontinuousScan() method of the RECOBeaconManager.
         * This method is to set whether the device scans BLE devices continuously or discontinuously.
         * The default is set as FALSE. Please set TRUE only for specific devices.
         *
         * mRecoManager.setDiscontinuousScan(true);
         */

        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    protected void stop(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServiceFail(RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        Log.i("RECORangingActivity", "error code = " + errorCode);
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }

    public class RECOListTask extends AsyncTask<Void, Void, ArrayList<RECOBeaconRegion>> {

        private String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
        private ArrayList<RECOBeaconRegion> regions;

        private String receiveMsg;
        private JSONArray jsonArray;

        private String error;

        @Override
        protected ArrayList<RECOBeaconRegion> doInBackground(Void... params) {
            Log.d("체크", "doInBackground");

            // ★android.permission.INTERNET 퍼미션 없을 시 Exception
            try {
                regions = new ArrayList<>();

                URL url = new URL("http://123.214.204.211:8080/Capstone2/main/recolist.jsp");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST"); // 전송 방식
                conn.setConnectTimeout(1000);

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

                        JSONObject jsonObject;

                        for(int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);

                            regions.add(new RECOBeaconRegion(RECO_UUID, jsonObject.getInt("major"), jsonObject.getString("beacon_num")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("buffer : ", buffer.toString());

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러"); // 에러 코드 확인용
                }

                conn.disconnect();

            } catch (SocketTimeoutException e) {
                error = "Timeout";
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return regions;
        }

        @Override
        protected void onPostExecute(ArrayList<RECOBeaconRegion> recoBeaconRegions) {
            super.onPostExecute(recoBeaconRegions);

            if(error != null && error.equals("Timeout")) {
                new AlertDialog.Builder(RECOActivity.this)
                        .setMessage("서버 연결 문제가 발생하였습니다.\n확인 버튼을 누르면 종료됩니다.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoadingActivity.loadingActivity.finish();
                                finish();
                                System.exit(0);
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }).show();
            }
        }
    }
}
