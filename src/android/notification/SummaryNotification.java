package de.appplant.cordova.plugin.notification;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;

import android.os.Build;
import android.support.v4.app.NotificationCompat;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;


public class SummaryNotification extends Notification {
    private static final String SUMMARY_FILENAME = "summary.txt";
    private static final String LOG_TAG = "SummaryNotification";

    public static int pending = 0;
	public static String tag = "SUMMARY";
    public static String text = "%d new items";

    public SummaryNotification(Context context, Options options,
                    NotificationCompat.Builder builder, Class<?> receiver){
        super(context, options, builder, receiver);
    }

	 /**
     * Present the local notification to user.
     */
    @Override
    public void show () {
        Log.d("LocalNotification", "SummaryNotification.show()");
        // TODO Show dialog when in foreground
        showNotification();
    }

    /**
     * Show as local notification when in background.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void showNotification () {
        // Clear all notifications before showing summary
        Log.d("LocalNotification", "SummaryNotification.showNotification()");
        Manager.getInstance(context).clearAll();

        if (Build.VERSION.SDK_INT <= 15) {
            // Notification for HoneyComb to ICS
            //getNotMgr().notify(tag, 0, builder.getNotification());
        } else {
            // Notification for Jellybean and above
            getNotMgr().notify(tag, 0, builder.build());
        }
    }

    /**
     * Clear the summary local notification.
     *
     */
    @Override
    public void clear () {
        Log.d("LocalNotification", "SummaryNotification.clear()");
        getNotMgr().cancel(tag, 0);
    }

    /**
     * Cancel the summary local notification.
     *
     * Create an intent that looks similar, to the one that was registered
     * using schedule. Making sure the notification id in the action is the
     * same. Now we can search for such an intent using the 'getService'
     * method and cancel it.
     */
    public void cancel() {
        Log.d("LocalNotification", "SummaryNotification.cancel()");
        Intent intent = new Intent(context, receiver)
                .setAction(SummaryNotification.tag);

        PendingIntent pi = PendingIntent.
                getBroadcast(context, 0, intent, 0);

        getAlarmMgr().cancel(pi);
        getNotMgr().cancel(tag, 0);

        unpersist();
    }

    // writes _count to the summary file and sets pending to _count
    public static void setSummaryCount(int _count, Context _context){
        try {
            Log.v(LOG_TAG, "Setting summary count to "+String.valueOf(_count));
            String path = _context.getExternalFilesDir(null).getAbsolutePath();
            File file = new File(path, SUMMARY_FILENAME);
            Log.e(LOG_TAG, file.toString());
            FileOutputStream stream = new FileOutputStream(file);

            while(_count > 255){
                stream.write(255);
                _count -= 255;
            }
            if(_count > 0){
                stream.write(_count);
            }
            stream.close();
            pending = _count;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // reads the summary file as a string, parses the contents as an integer, and returns the result
    public static int getSummaryCount(Context _context){
        int count = 0;
        try{


            String path = _context.getExternalFilesDir(null).getAbsolutePath();
            File file = new File(path, SUMMARY_FILENAME);
            FileInputStream in = new FileInputStream(file);
            int read;
            while((read = in.read()) != -1){          
                count += read;
                Log.v(LOG_TAG, "Read: "+String.valueOf(read));
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.toString());
            Log.e(LOG_TAG, "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.toString());
        }
        Log.v(LOG_TAG, "Current summary count: "+String.valueOf(count));
        return count;
    }

    // sets the summary count to the current summary count + 1
    public static void incrementSummaryCount(Context _context){
        int c = getSummaryCount(_context) + 1;
        setSummaryCount(c, _context);
    }
    // sets the summary count to the current summary count - 1
    public static void decrementSummaryCount(Context _context){
        int c = getSummaryCount(_context) - 1;
        setSummaryCount(c, _context);
    }
}