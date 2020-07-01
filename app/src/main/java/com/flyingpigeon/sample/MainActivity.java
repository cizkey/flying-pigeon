package com.flyingpigeon.sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.flyingpigeon.library.Config;
import com.flyingpigeon.library.Pigeon;
import com.flyingpigeon.library.ServiceManager;
import com.flyingpigeon.library.annotations.route;

import java.util.ArrayList;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Config.PREFIX + MainActivity.class.getSimpleName();

    Handler mHandler = new Handler(Looper.getMainLooper());
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.appName = this.findViewById(R.id.appName);


        Log.e(TAG, "MainActivity");
        final Pigeon pigeon = Pigeon.newBuilder(this).setAuthority(ServiceApiImpl.class).build();

        Short aShort = 1;
        byte aByte = 10;
        final IServiceApi serviceApi = pigeon.create(IServiceApi.class);
        serviceApi.queryTest(1);
        serviceApi.queryItems(UUID.randomUUID().hashCode(), 0.001D, SystemClock.elapsedRealtime(), aShort, 0.011F, aByte, true);
        Information information = new Information("Justson", "just", 110, (short) 1, 'c', 1.22F, (byte) 14, 8989123.111D, 100000L);
        serviceApi.submitInformation(UUID.randomUUID().toString(), 123144231, information);


        Poster resultPoster = serviceApi.queryPoster(UUID.randomUUID().toString());
        Log.e(TAG, "resultPoster:" + GsonUtils.toJson(resultPoster));

        testReturn(serviceApi);
        ArrayList data = new ArrayList<String>();
        data.add("test1");
        data.add("test2");
        serviceApi.testArrayList(data);


        Poster poster = new Poster("Justson", "just", 119, 11111000L, (short) 23, 1.15646F, 'h', (byte) 4, 123456.415D);
        int posterId = serviceApi.createPoster(poster);
        Log.e(TAG, "posterId:" + posterId);

//        Log.e(TAG, "returnResult:" + returnResult);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                test(pigeon);
                pigeon.route("/words").withString("name", "Justson").fly();
                pigeon.route("/hello").with(new Bundle()).fly();
                pigeon.route("/world").fly();
                pigeon.route("/world/error").fly();
                pigeon.route("/submit/bitmap", UUID.randomUUID().toString(), new byte[1024 * 1000 * 3], 1200).resquestLarge().fly();

                int resquestLargeResult = pigeon.route("/submit/bitmap2", UUID.randomUUID().toString(), new byte[1024 * 1000 * 3], 1200).resquestLarge().<Integer>fly();
                Log.e(TAG, "resquestLargeResult:" + resquestLargeResult);
                byte[] data = pigeon.route("/query/bitmap", "girl.jpg", 5555).responseLarge().fly();
                if (null != data) {
                    //Arrays.toString(data)
                    Log.e(TAG, "data length:" + data.length);
                } else {
                    Log.e(TAG, "data is null.");
                }

                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 1; i++) {
                            SystemClock.sleep(50);
                            String returnResult = serviceApi.testLargeBlock("hello,worlds ", new byte[1024 * 1024 * 3]);
                            Log.e(TAG, "returnResult:" + returnResult);
                        }
                    }
                });
            }
        }, 400);

        // 跨应用通信
        this.findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pigeon flyPigeon = Pigeon.newBuilder(MainActivity.this).setAuthority("com.flyingpigeon.ipc_sample").build();
                Bundle bundle = flyPigeon.route("/query/username").withString("userid", UUID.randomUUID().toString()).fly();
                if (bundle != null) {
                    Log.e(TAG, "bundle:" + bundle.toString());
                    appName.setText(bundle.getString("username"));
                } else {
                    Log.e(TAG, "bundle == null");
                }
            }
        });

        ServiceManager.getInstance().publish(this);
    }

    @route("/show/myapp/name")
    public void showMyAppName(final Bundle in, Bundle out) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String name = in.getString("name");
                appName.setText(name);
            }
        });
        out.putString("name", "fly-pigeon");
    }

    private void test(Pigeon pigeon) {
        Short aShort = 1;
        byte aByte = 10;
        Api api = pigeon.create(Api.class);
        Poster poster1 = new Poster("Justson", "just", 119, 11111000L, (short) 23, 1.15646F, 'h', (byte) 4, 123456.415D);
        api.createPoster(poster1);

        RemoteServiceApi remoteServiceApi = pigeon.create(RemoteServiceApi.class);
        remoteServiceApi.queryItems(UUID.randomUUID().hashCode(), 0.001D, SystemClock.elapsedRealtime(), aShort, 0.011F, aByte, true);

        byte[] bitmapData = remoteServiceApi.testLargeResponse("query Bitmap");
        if (bitmapData != null) {
            Log.e(TAG, "bitmapData:" + bitmapData.length);
        } else {
            Log.e(TAG, "bitmapData is null");
        }

    }

    private void testReturn(IServiceApi serviceApi) {
        Poster poster = new Poster("Justson", "just", 119, 11111000L, (short) 23, 1.15646F, 'h', (byte) 4, 123456.415D);

//        Log.e(TAG, "int:" + serviceApi.createPoster(poster) + " double:" + serviceApi.testDouble() + " long:" + serviceApi.testLong() + " short:" + serviceApi.testShort() + " float:" + serviceApi.testFloat() + " byte:" + serviceApi.testByte() + " boolean:" + serviceApi.testBoolean() + " testParcelable:" + GsonUtils.toJson(serviceApi.testParcelable()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().unpublish(this);
    }
}
