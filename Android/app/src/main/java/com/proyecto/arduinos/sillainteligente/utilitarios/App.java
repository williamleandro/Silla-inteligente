package com.proyecto.arduinos.sillainteligente.utilitarios;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

//CLASE QUE CREA LOS CANALES DE NOTIFICACIONES NECESARIOS PARA LANZARLOS
public class App extends Application {
    private static final String CHANNEL_NOTIF_LED_ID = "Led_Uno";
    private static final String CHANNEL_NOTIF_LED_ID_2 = "Led_Dos";

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalNotificacion();
    }

    private void crearCanalNotificacion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canalNotificacionLED = new NotificationChannel(
                    CHANNEL_NOTIF_LED_ID, "CANAL_LED", NotificationManager.IMPORTANCE_HIGH);
            canalNotificacionLED.setDescription("Este es el canal LED.");

            NotificationManager managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            managerNotification.createNotificationChannel(canalNotificacionLED);

            NotificationChannel canalNotificacionAlarma = new NotificationChannel(
                    CHANNEL_NOTIF_LED_ID_2, "CANAL_ALARMA", NotificationManager.IMPORTANCE_HIGH);
            canalNotificacionAlarma.setDescription("Este es el canal LED.");

            NotificationManager managerNotificationAlarma = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            managerNotificationAlarma.createNotificationChannel(canalNotificacionAlarma);
        }
    }
}
