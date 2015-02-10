package org.mes.hack.pollarizing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmReceiver extends WakefulBroadcastReceiver {
    public GcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startActivity = new Intent(context, HomeActivity.class);
        startActivity.putExtra("select_tab", 2);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startActivity, 0);

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        String data = intent.getStringExtra("data");
        if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification n = new Notification.Builder(context)
                    .setContentTitle("You've been POLL'd")
                    .setContentText(data != null ? data : "Click to open!")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            nm.notify(500, n);
        }
    }
}
