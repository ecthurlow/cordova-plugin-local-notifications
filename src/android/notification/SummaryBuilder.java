/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package de.appplant.cordova.plugin.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import android.util.Log;
/**
 * Builder class for summary local notifications. Build fully configured local
 * notification specified by JSON object passed from JS side.
 */
public class SummaryBuilder extends Builder{
    /**
     * Constructor
     *
     * @param context
     *      Application context
     * @param options
     *      Notification options
     */
    public SummaryBuilder(Context context, JSONObject options) {
        super(context, options);
    }

    /**
     * Creates the notification with all its options passed through JS.
     */
    public SummaryNotification build() {
        Log.d("LocalNotification", "SummaryBuilder.build()");
         // Insert pending into text, if slot is defined
        String summaryText = new String(options.getText()).replace("%d", Integer.toString(SummaryNotification.pending));

        Uri sound = options.getSoundUri();
        NotificationCompat.BigTextStyle style;
        NotificationCompat.Builder builder;

        style = new NotificationCompat.BigTextStyle()
                .bigText(summaryText);

        builder = new NotificationCompat.Builder(context)
                .setDefaults(0)
                .setContentTitle(options.getTitle())
                .setContentText(summaryText)
                .setNumber(SummaryNotification.pending)
                .setTicker(summaryText)
                .setSmallIcon(options.getSmallIcon())
                .setLargeIcon(options.getIconBitmap())
                .setAutoCancel(options.isAutoClear())
                .setOngoing(options.isOngoing())
                .setStyle(style)
                .setLights(options.getLedColor(), 500, 500);

        if (sound != null) {
            builder.setSound(sound);
        }

        applyDeleteReceiver(builder);
        applyContentReceiver(builder);

        return new SummaryNotification(context, options, builder, triggerReceiver);
    }

    private String bundleOptions(){

        try {
            JSONObject jsonOpts = new JSONObject(options.toString());
            jsonOpts.put("isSummary", true);
            return jsonOpts.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return options.toString();
    }

    /**
     * Set intent to handle the delete event. Will clean up some persisted
     * preferences.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyDeleteReceiver(NotificationCompat.Builder builder) {
        Log.d("LocalNotification", "SummaryBuilder.applyDeleteReceiver()");
        if (clearReceiver == null)
            return;


        Intent deleteIntent = new Intent(context, clearReceiver)
                .setAction(SummaryNotification.tag)
                .putExtra(Options.EXTRA, bundleOptions());

        PendingIntent dpi = PendingIntent.getBroadcast(
                context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setDeleteIntent(dpi);
    }

    /**
     * Set intent to handle the click event. Will bring the app to
     * foreground.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyContentReceiver(NotificationCompat.Builder builder) {
        Log.d("LocalNotification", "SummaryBuilder.applyContentReceiver()");
        if (clickActivity == null)
            return;

        Intent intent = new Intent(context, clickActivity)
                .putExtra(Options.EXTRA, bundleOptions())
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        int requestCode = new Random().nextInt();

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(contentIntent);
    }


}
