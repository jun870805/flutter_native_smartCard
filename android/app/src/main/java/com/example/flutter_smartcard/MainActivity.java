package com.example.flutter_smartcard;

import io.flutter.embedding.android.FlutterActivity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.content.Intent;

import com.infothink.ccid.IT100MUR;
import com.infothink.smartcard.Card;
import com.infothink.smartcard.HealthCard;
import com.infothink.smartcard.MifareClassic;
import com.infothink.smartcard.SCException;
import com.infothink.util.IT100MURActivity;


public class MainActivity extends IT100MURActivity{

    public 	static final int	READER_TYPE_IC	= 0;
    public 	static final int	READER_TYPE_NFC	= 1;
    public	boolean	mCardReady	= 	false;
    public	int	    mReaderType		= READER_TYPE_IC;
    private	MainBroadcastRecv	mMainRecv		= new MainBroadcastRecv();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DebugCommand();
        UseNFC(mReaderType == READER_TYPE_NFC);
        ExecuteCard();
        createSCThread();

        mMainRecv.setMainActivityHandler(this);
        registerReceiver(mMainRecv,	 new IntentFilter("android.intent.action.AES_SERVICE"));

        //開啟flutter頁面
        Intent intent = new Intent(this, FlutterMainActivity.class);
        startActivity(intent);
    }
    @Override
    protected IT100MURActivity.SCThread createSCThread() {

        System.out.println("createSCThread");
        return new SCThread();
    }

    //----- handleSCMessage() ------------------------------
    //	handleSCMessage()
    //	接收讀卡機, 卡片運作過程的訊息通知, 這裡可以進行相對應的UI或訊息處理
    //	訊息通知按照讀卡機運作順序包含:
    //	WAITING_READER_CONNECT: 等待讀卡機連線
    //	READER_CONNECTED: 完成讀卡機連線
    //	WAITING_CARD_INSERT: 等待插卡或偵測感應卡靠近
    //	WAITING_CARD_REMOVE: 等待拔卡或偵測感應卡離開
    //	CARD_INSERTED: 完成插卡或偵測感應卡靠近
    //	CARD_REMOVED: 完成拔卡或偵測感應卡離開
    //	RUN_COMMANDS_AFTER: 準備執行 RunCommands()函式
    //	RUN_COMMANDS_BEFORE: 完成執行 RunCommands()函式
    //	RUN_COMMANDS_ERROR: 執行過程中有任何 Exception產生Error
    //------------------------------------------------------
    @Override
    protected void handleSCMessage(Message msg)
    {
        Events what = Events.values()[msg.what];

        switch (what)
        {
            case WAITING_READER_CONNECT:
                //System.out.println("Smcard --> WAITING_READER_CONNECT");
                break;
            case READER_CONNECTED:
                System.out.println("Smcard --> READER_CONNECTED, mCardReady = " + mCardReady);
                break;
            case WAITING_CARD_INSERT:
                //System.out.println("Smcard --> WAITING_CARD_INSERT");
                break;
            case CARD_INSERTED:
                System.out.println("Smcard --> CARD_INSERTED.");
                mCardReady = true;
                break;
            case WAITING_CARD_REMOVE:
                //System.out.println("Smcard --> WAITING_CARD_REMOVE");
                break;
            case CARD_REMOVED:
            {
                System.out.println("Smcard --> CARD_REMOVED, mCardReady = " + mCardReady);
                try {	Thread.sleep(500);	} catch (InterruptedException e) { }
                mCardReady = false;

                if (mReaderType==READER_TYPE_IC)
                {

                }
                ExecuteCard();
                break;
            }
            case RUN_COMMANDS_BEFORE:
                //System.out.println("Smcard --> RUN_COMMANDS_BEFORE");
                break;
            case RUN_COMMANDS_AFTER:
                //System.out.println("Smcard --> RUN_COMMANDS_AFTER");
                break;
            case RUN_COMMANDS_ERROR:
            {
                System.out.println("Smcard --> RUN_COMMANDS_ERROR, mCardReady = " + mCardReady);
                //Exception e = (Exception)msg.obj;
                //System.out.println(e.getMessage());
                if (mCardReady)
                {
                    try {	Thread.sleep(1000);	} catch (InterruptedException e) { }
                    mCardReady = false;
                    ExecuteCard();
                }
                break;
            }
            default:
                break;
        }
    }

    //----- getHexString() ---------------------------------
    private String getHexString(byte[] bytes, int type)
    {
        StringBuilder sb = new StringBuilder();
        //for (int i = bytes.length - 1; i >= 0; --i)
        for (int i=0; i<bytes.length; i++)
        {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b).toUpperCase() + ((type==0)?" ":""));
            //if (i > 0)
            //	sb.append(" ");
        }

        return sb.toString();
    }

    //******************************************************
    //**	class SCThread - 裡面處理讀取卡片相關作業, 需實作 RunCommands 函式
    //**	當偵測到卡片時會自動呼叫 RunCommands
    //******************************************************
    class SCThread extends IT100MURActivity.SCThread
    {
        //--------------------------------------------------
        //	RunCommands() 執行卡片相關作業(指令集)
        //	偵測到插卡後會呼叫此函式 , 此函式在 Thread 中呼叫, 請注意 Thread 的使用限制條件
        //	例如: Thread 中要處理 UI, 需透過 runOnUiThread的方式
        //--------------------------------------------------
        @Override
        protected void RunCommands() throws SCException
        {
            System.out.println("RunCommands(), Step 1, mCardReady = " + mCardReady);

            if (mReaderType==READER_TYPE_IC)
            {
                HealthCard card = null;
                boolean		init = false;

                try {
                    card = (HealthCard)ConnectCard("com.infothink.smartcard.HealthCard", Card.PROTOCOL_ANY);
                    init = true;
                } catch (SCException e) { }

                if (card==null || init==false)
                {
                    WaitCardRemoved();
                    if (card!=null)
                        card.Disconnect();
                    return;
                }

                card.ReadCard();

                String CardData = "卡號:" + card.CardNumber + "\n" + "姓名:" + card.Name + "\n" + "身分證號碼:" + card.ID + "\n" + "出生日期:" + card.BirthDay + "\n" + "姓名:" + card.Gender + "\n" + "發卡日期:" + card.IssuedDate;
                System.out.println(CardData);

                WaitCardRemoved();
                card.Disconnect();
            }
            else
            {
                MifareClassic card = null;
                boolean			init = false;

                try {
                    card = (MifareClassic)ConnectCard("com.infothink.smartcard.MifareClassic", Card.PROTOCOL_ANY);
                    init = true;
                } catch (SCException e)	{ }

                if (card==null || init==false)
                {
                    WaitCardRemoved();
                    if (card!=null)
                        card.Disconnect();
                    return;
                }

                BuzzerControl(IT100MUR.BUZZER_ON);
                try {	sleep(200);	} catch (InterruptedException e) { }
                BuzzerControl(IT100MUR.BUZZER_OFF);

                System.out.println(getHexString(card.GetCardID(), 1));

                WaitCardRemoved();
                if (card!=null)
                    card.Disconnect();
            }
        }
    }


    //****************************************************************
    //**	Service
    //****************************************************************

    //-----	Service_Set_Param()	----------------------------
    //	Service	want to	SET	cmd & param to activity
    //------------------------------------------------------
    public void	Service_Set_Param(String cmd, String param, String param2, String param3, int akey)
    {
        System.out.println(String.format("Tracker : Service_Set_Param(), cmd = %s, param = %s, param2 = %s, param3 = %s, akey = %s", cmd, param, param2, param3, akey));
        System.out.println("姓名: "+param2+" ;身分證: "+param);
        sendFlutterMessage(param);
        if (cmd.equals("kill"))
        {
            //finish();
            System.exit(0);
            //onDestroy();
        }
    }


    //-----	Service_Get_Param()	----------------------------
    //	Service	want to	GET	param from activity
    //------------------------------------------------------
    public void	Service_Get_Param(String cmd, String param, String param2, int akey)
    {
        System.out.println(String.format("Tracker : Service_Get_Param(), cmd = %s, param = %s, param2 = %s, akey = %s", cmd, param, param2, akey));

        String	value = "the answer of service want...";
    }

    private void sendFlutterMessage(String idNumber) {
        Intent lIntent = new Intent("android.to.flutter");
        lIntent.putExtra("IDNumber", idNumber);
        sendBroadcast(lIntent);
    }

}



