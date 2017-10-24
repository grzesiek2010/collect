package org.odk.collect.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class ElevationReader extends AsyncTask<LatLng, Object, String> {

    private final ProgressDialog mDialog;
    private Context mContext;
    private AsyncResponse mDelegate = null;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public ElevationReader(Context context, AsyncResponse delegate) {
        mContext = context;
        mDialog = new ProgressDialog(context);
        mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        mDialog.setCancelable(false);
        mDialog.setMessage(mContext.getString(R.string.reading_location));
        mDialog.show();
    }

    @Override
    protected String doInBackground(LatLng... params) {
        String elevation = null;
        URL url;
        try {
            url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations=" +
                    String.valueOf(params[0].latitude) + "," +
                    String.valueOf(params[0].longitude) +
                    "&AIzaSyCnGUUz7DEX7GOf1BpRFEJS9gAKOdhpN08");

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            urlConnection.setRequestProperty("Accept", "application/json");

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);

            try {
                if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        json.append(line);
                    }

                    JSONObject jroot = new JSONObject(json.toString());
                    if ("OK".equals(jroot.getString("status"))) {
                        JSONArray results = jroot.getJSONArray("results");
                        if (results.length() > 0) {
                            NumberFormat nf = NumberFormat.getInstance(Locale.US);
                            nf.setMaximumFractionDigits(1);
                            nf.setGroupingUsed(false);
                            elevation = nf.format(results.getJSONObject(0).getDouble("elevation"));
                        }
                    }
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (JSONException | IOException e) {
            Timber.e(e);
        }
        return elevation;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        mDelegate.processFinish(result);
    }
}