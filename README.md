# Flutter 串接Java原生碼

Flutter && Android java (for InfoThink IT100U SmartCard)

實作內容：讀卡機SDK
與原生碼溝通：EventChannel

## Step1 添加jar檔至 android/libs/ 下，並引用 ：

引用SDK android/app/build.gradle

    dependencies {
        implementation fileTree(dir: "libs", include: ["*.jar"])
    }

## Step2 Java端實作 ：

AndroidManifest.xml 寫入頁面

    <activity
           android:name=".FlutterMainActivity"
           android:theme="@android:style/Theme.NoTitleBar" />

APP啟動時先讓讀卡機啟動，並開機廣播監聽讀卡機的變動 MainActivity.java

    @Override
    public void onCreate(Bundle savedInstanceState) {  
        //啟動讀卡機
        createSCThread();

        mMainRecv.setMainActivityHandler(this);
        registerReceiver(mMainRecv,	 new IntentFilter("android.intent.action.AES_SERVICE"));

        //開啟flutter頁面
        Intent intent = new Intent(this, FlutterMainActivity.class);
        startActivity(intent);
    }

    //當讀卡機讀到變動時，通知 FlutterMainActivity.java 發送訊息給flutter端
    private void sendFlutterMessage(String idNumber) {
        Intent lIntent = new Intent("android.to.flutter");
        lIntent.putExtra("IDNumber", idNumber);
        sendBroadcast(lIntent);
    }

接著跳頁到 FlutterMainActivity.java

    //進行第一次的宣告 "flutter_and_native"是與flutter端溝通的key
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
                    public void onCancel(Object o) {}
                }
        );
    }

    //監聽MainActivity.java的廣播，並發送訊息給flutter端
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

## Step3 flutter端實作：

main.dart 

    static const EventChannel _channel = EventChannel('flutter_and_native');
    late dynamic _streamSubscription;

    //開始監聽Java的呼叫 event 就是收到的Map
    _streamSubscription = _channel.receiveBroadcastStream().listen((dynamic event) {
        debugPrint('Received event: $event');
        if(event['message'].toString()!="null"){
            changeMessage = event['message'].toString();
        }
    },onError: (dynamic error) {
        debugPrint('Received error: ${error.message}');
    }, cancelOnError: true);

    //關閉監聽
    _streamSubscription.cancel();