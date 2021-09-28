package com.example.flutter_smartcard;

import io.flutter.embedding.android.FlutterActivity;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.EventChannel;


public class FlutterMainActivity extends FlutterActivity{

    Handler mHandler = new Handler(Looper.myLooper());
    private EventChannel.EventSink mEventSink;
    private MyReceiver mMyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //EventChannel 通信方式
        eventChannelFunction();

        mMyReceiver = new MyReceiver();
        IntentFilter lIntentFilter = new IntentFilter("android.to.flutter");
        registerReceiver(mMyReceiver, lIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMyReceiver);
    }

    private void eventChannelFunction(){
        EventChannel lEventChannel = new EventChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), "flutter_and_native");
        lEventChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink eventSink) {
                        mEventSink = eventSink;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("message", "注册成功");
                                resultMap.put("code", 200);
                                eventSink.success(resultMap);
                            }

                        });
                    }

                    @Override
                    public void onCancel(Object o) {

                    }


                }
        );
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String idNumber = intent.getExtras().getString("IDNumber");
            Toast.makeText(context, "接收到讀卡機的變化", Toast.LENGTH_SHORT).show();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> resultMap2 = new HashMap<>();
                    resultMap2.put("message", idNumber);
                    resultMap2.put("code", 200);

                    if (mEventSink != null) {
                        mEventSink.success(resultMap2);
                    }
                }
            });
        }
    }

}