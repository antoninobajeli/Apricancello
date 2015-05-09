package com.abajeli.beaconcadabra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by abajeli on 02/05/15.
 */
public class NotificationActivity extends Activity{

    static String TAG="NotificationActivity";
    ImageView mImageView;
    TextView mTextView;
    //R.string.
    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mImageView = (ImageView) findViewById(R.id.image_view);
        mTextView = (TextView) findViewById(R.id.text_view);

        Intent intent = getIntent();
        if (intent != null) {
            //mTextView.setText(intent.getStringExtra(EXTRA_TITLE));
            //final Asset asset = intent.getParcelableExtra(EXTRA_IMAGE);
            //loadBitmapFromAsset(this, asset, mImageView);
            mTextView.setText("wwwww");
        }
    }

    public static void loadBitmapFromAsset(final Context context,
                                           final Asset asset, final ImageView target) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(Wearable.API)
                        .build();
                ConnectionResult result =
                        googleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }

                // convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        googleApiClient, assets[0]).await().getInputStream();
                googleApiClient.disconnect();

                if (assetInputStream == null) {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }

                // decode the stream into a bitmap
                return BitmapFactory.decodeStream(assetInputStream);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    target.setImageBitmap(bitmap);
                }
            }
        }.execute(asset);
    }

}
