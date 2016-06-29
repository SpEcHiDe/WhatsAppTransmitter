package me.shrimadhavuk.watransmitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by maxi on 19/06/2016.
 */
public class AutoArranque extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, GooogleServ.class);
        context.startService(serviceIntent);
    }
}
