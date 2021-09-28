package com.example.flutter_smartcard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;


//********************************************************************
//**	class MainBroadcastRecv
//********************************************************************
public class MainBroadcastRecv extends BroadcastReceiver
{
	public	static final String	BCSTR_SRV_GETPARAM	= "srv_get_param";		// Service want to GET param from activity
	public	static final String	BCSTR_SRV_SETPARAM	= "srv_set_param";		// Service want to SET param  to  activity
	public	static final String	BCSTR_ACT_GETPARAM	= "act_get_param";		// Activity want to ASK param  from Service, return param from boradcast's request

	private	MainActivity	mAct = null;


	//----- setMainActivityHandler() -----------------------
	public void setMainActivityHandler(MainActivity act)
	{
		mAct = act;
	}


	//----- onReceive() ------------------------------------
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String	action = intent.getAction();

		if (action.equals("android.intent.action.AES_SERVICE"))
		{
			Bundle	bnd		= intent.getExtras();
			String	op		= bnd.getString("Op");
			String	cmd		= bnd.getString("Cmd");
			String	param	= bnd.getString("Param");
			String	param2	= bnd.getString("Param2");
			String	param3	= bnd.getString("Param3");

			if (mAct!=null)
			{
				int		akey	= bnd.getInt("AKey");							// a random number, for sequence process

				if (op.equals(BCSTR_SRV_GETPARAM))								// Service want to GET param from activity, send a broadcast
				{
					mAct.Service_Get_Param(cmd, param, param2, akey);
				}
				else if (op.equals(BCSTR_SRV_SETPARAM))							// Service want to SET param  to  activity, send a broadcast
				{
					mAct.Service_Set_Param(cmd, param, param2, param3, akey);
				}
			}
		}
		else if (action.equals(Intent.ACTION_BOOT_COMPLETED))
		{
			System.out.println("InfoService, ACTION_BOOT_COMPLETED........");
			//Intent mainact = new Intent(context, MainActivity.class);
			//mainact.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//context.startActivity(mainact);
		}
		else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
		{
			System.out.println("InfoService, ACTION_USB_DEVICE_ATTACHED........");
		}
	}
}
