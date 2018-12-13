package abc.integratedtest2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by HAN on 2017-10-22.
 */

public class LoadingActivity extends Activity{

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 10;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private DeviceUuidFactory duf;

    protected static Activity loadingActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingActivity = LoadingActivity.this;

        Log.d("LoadingActivity", "onCreate");

        if(!isTaskRoot()) {
            finish();
            return;
        }

        // 초기화
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("beforeOrderPrice", 0);
        editor.putString("beforeSingleCount", "0");
        editor.putString("beforeSetCount", "0");
        editor.putString("beforeSideCount", "0");
        editor.putInt("selectedOrderPrice", 0);
        editor.putString("selectedSingleCount", "0");
        editor.putString("selectedSetCount", "0");
        editor.putString("selectedSideCount", "0");
        editor.putInt("orderCount", 0);
        editor.putInt("beforeCount", 0);
        editor.apply();

        duf = new DeviceUuidFactory(this);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("LoadingActivity", "BluetoothManager, Adapter");
        // bluetooth 체크
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter.isEnabled()) {
            locationPermission();
        } else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }

        //locationPermission(); // 테스트용(위치 허용 후 넘기기)

        // 테스트용(그냥 넘기기)
        /*Log.d("LoadingActivity", "Handler");
        Handler handle = new Handler();
        Runnable delay = new Runnable() {
            public void run() {
                nextActivity();
            }
        };
        handle.postDelayed(delay,1500);*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void locationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is not granted.");
                this.requestLocationPermission();
            } else {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is already granted.");
                nextActivity();
            }
        } else nextActivity();
    }

    public void nextActivity() {
        Intent intent = new Intent(this, RECOActivity.class);
        intent.putExtra("isLoading", true);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private void requestLocationPermission() {
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("권한 요청")
                .setMessage("앱에서 위치에 대한 권한을 요청합니다.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoadingActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION : {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    nextActivity();
                } else {
                    Toast.makeText(this, "권한이 거절되었습니다. 기능이 제대로 작동하지 않을 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            default :
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //If the request to turn on bluetooth is denied, the app will be finished.
            //사용자가 블루투스 요청을 허용하지 않았을 경우, 어플리케이션은 종료됩니다.
            finish();
            return;
        } else if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            locationPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
