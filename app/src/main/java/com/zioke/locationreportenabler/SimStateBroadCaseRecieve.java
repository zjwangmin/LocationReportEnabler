package com.zioke.locationreportenabler;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by MinWang on 2017/3/3.
 */

public class SimStateBroadCaseRecieve extends BroadcastReceiver {
    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static int SIM_VALID = 0;
    private final static int SIM_INVALID = 1;
    private int simState = SIM_INVALID;

    private final String COMMAND_PREFIX = "setprop ";
    private final String[] PROPERTIES = {"gsm.sim.operator.numeric 300012"/*310004*/,
            "gsm.sim.operator.iso-country us",
            "gsm.sim.operator.alpha Verizon"};

    public int getSimState() {
        return simState;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("sim state changed");
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            int state = tm.getSimState();
            switch (state) {
                case TelephonyManager.SIM_STATE_READY :
                    simState = SIM_VALID;
                    enableLocationReport();
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN :
                case TelephonyManager.SIM_STATE_ABSENT :
                case TelephonyManager.SIM_STATE_PIN_REQUIRED :
                case TelephonyManager.SIM_STATE_PUK_REQUIRED :
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
                default:
                    simState = SIM_INVALID;
                    break;
            }
        }
    }

    public void enableLocationReport() {
        try{
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String property : PROPERTIES) {
                os.writeBytes(COMMAND_PREFIX + property + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}