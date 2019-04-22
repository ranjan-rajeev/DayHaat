package com.dayhaat.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dayhaat.MainActivity;
import com.dayhaat.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /****************************************************
     (Key)           (Action)
     NL >>>>>>>>>  Notification List
     WB >>>>>>>   WebView
     MSG >>>>>>>  Notification MESSAGE
     Default >>>  Home Page
     **************************************************/
    private NotificationManager mManager;
    private static final String TAG = "FirebaseIDService";
    public static final String CHANNEL_ID = "com.dayhaat.NotifyID";
    public static final String CHANNEL_NAME = "DAYHAAT";
    public static final int REQUEST_CODE = 4567;

    Bitmap bitmap_image = null;
    String type;
    String WebURL, WebTitle, messageId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage);
    }

    public int getNotificationId() {
        return (int) Math.round(Math.random() * 10000);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent;
        String title = "DayHaat", body = "", url = "https://www.dayhaat.com/", banner;
        if (remoteMessage != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Data  :" + data.toString());
            Log.d(TAG, "Title : " + notification.getTitle());
            Log.d(TAG, "Body : " + notification.getBody());

            title = notification.getTitle();
            body = notification.getBody();

            if (data.get("url") != null) {
                url = data.get("url");
            }
            if (data.get("banner") != null) {
                banner = data.get("banner");
                bitmap_image = getBitmapfromUrl(banner);
                //new createBitmapFromURL(banner).execute();
            }
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("URL", url);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            createChannels();

            NotificationCompat.BigPictureStyle BigPicstyle = new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap_image)
                    .setBigContentTitle(title)
                    .setSummaryText(body)
                    .bigLargeIcon(null);

            NotificationCompat.BigTextStyle BigTextstyle = new NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
                    .setSummaryText(body);

            NotificationCompat.Builder notificationBuilder = null;


            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            if (bitmap_image != null) {
                notificationBuilder.setStyle(BigPicstyle);
                notificationBuilder.setLargeIcon(bitmap_image);
            } else {
                notificationBuilder.setStyle(BigTextstyle);
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
            } else {
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            }


            //region setting notification


            notificationBuilder
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setTicker("DayHaat")
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setWhen(System.currentTimeMillis())
                    .setVisibility(getNotificationId())
                    .setChannelId(CHANNEL_ID)
                    .setNumber(1)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setContentIntent(pendingIntent);
            //endregion

            getManager().notify(getNotificationId(), notificationBuilder.build());

        }


    }

    //   .setStyle(new NotificationCompat.BigTextStyle().bigText(NotifyData.get("body")))
    //      builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.finmart_logo_splash));


    /*private void setNotifyCounter() {
        int notifyCounter = prefManager.getNotificationCounter();
        prefManager.setNotificationCounter(notifyCounter + 1);

        Intent intent = new Intent(Utility.PUSH_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


    }*/

    public void createChannels() {
        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            channel.setDescription("DayHaat");
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);    // Notification.VISIBILITY_PRIVATE
            getManager().createNotificationChannel(channel);
        }
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {

            if (imageUrl.trim().equals("")) {
                return null;
            }
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    public class createBitmapFromURL extends AsyncTask<Void, Void, Bitmap> {
        URL NotifyPhotoUrl;
        String imgURL;

        public createBitmapFromURL(String imgURL) {
            this.imgURL = imgURL;
        }


        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap networkBitmap = null;

            try {
                NotifyPhotoUrl = new URL(imgURL);
                networkBitmap = BitmapFactory.decodeStream(
                        NotifyPhotoUrl.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Could not load Bitmap from: " + NotifyPhotoUrl);
            }

            return networkBitmap;
        }

        protected void onPostExecute(Bitmap result) {

            bitmap_image = result;
        }
    }
}