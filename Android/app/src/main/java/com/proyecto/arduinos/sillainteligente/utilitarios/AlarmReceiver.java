package com.proyecto.arduinos.sillainteligente.utilitarios;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.proyecto.arduinos.sillainteligente.R;

public class AlarmReceiver extends BroadcastReceiver {
    private NotificationManagerCompat notificationManagerCompat;
    private static final String CHANNEL_NOTIF_LED_ID_2 = "Led_Dos";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.notificationManagerCompat = NotificationManagerCompat.from(context);

        crearNotificacion(context);

    }

    private void crearNotificacion(Context context) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_NOTIF_LED_ID_2)
                .setSmallIcon(R.drawable.run)
                .setContentTitle("Aviso - Tiempo Inactivo")
                .setContentText("Estuvo mucho tiempo inactivo.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

        notificationManagerCompat.notify(1, notification);
    }
}
