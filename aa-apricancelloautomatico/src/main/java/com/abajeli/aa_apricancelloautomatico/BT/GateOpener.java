package com.abajeli.aa_apricancelloautomatico.BT;

/**
 * Created by abajeli on 18/04/15.
 */
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class GateOpener extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "com.abajeli.aa_apricancelloautomatico.gateopener.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update all active widgets
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Used when the widget is first created
    }

    @Override
    public void onDisabled(Context context) {
        // Used when widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.gate_opener);
        //Intent intent = new Intent(WIDGET_BUTTON);
        ///PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //views.setOnClickPendingIntent(R.id.btnOpen, pendingIntent);

        //appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
