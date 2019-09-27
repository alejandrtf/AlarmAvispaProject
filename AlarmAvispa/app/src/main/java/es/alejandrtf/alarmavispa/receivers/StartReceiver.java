package es.alejandrtf.alarmavispa.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.alejandrtf.alarmavispa.services.AlarmAvispaFCMService;

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, AlarmAvispaFCMService.class));
    }
}
