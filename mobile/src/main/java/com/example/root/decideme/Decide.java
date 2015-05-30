package com.example.root.decideme;

import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


public class Decide extends ActionBarActivity {

    public static final String MIXPANEL_TOKEN = "sm_android";
    private MixpanelAPI mMixpanel;
    private static final String MIXPANEL_DISTINCT_ID_NAME = "Mixpanel Example $distinctid";
    private boolean newUser = true;

    private RadioGroup venueGroup;
    private RadioButton venueButton;
    private Button btnDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decide);
        final String trackingDistinctId = getTrackingDistinctId();

        addListenerOnButton();

    // Initialize the library with your
    // Mixpanel project token, MIXPANEL_TOKEN, and a reference
    // to your application context.
        mMixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        mMixpanel.identify(trackingDistinctId); //this is the distinct_id value that
        // will be sent with events. If you choose not to set this,
        // the SDK will generate one for you

        mMixpanel.getPeople().identify(trackingDistinctId);

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            final JSONObject properties = new JSONObject();
            properties.put("new user", newUser);
            properties.put("user domain", "(unknown)"); // default value
            mMixpanel.registerSuperPropertiesOnce(properties);
        } catch (final JSONException e) {
            throw new RuntimeException("Could not do the thing");
        }

        mMixpanel.track("App Resumed", null);
        newUser = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_decide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

    private String getTrackingDistinctId() {
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        String ret = prefs.getString(MIXPANEL_DISTINCT_ID_NAME, null);
        if (ret == null) {
            ret = generateDistinctId();
            final SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString(MIXPANEL_DISTINCT_ID_NAME, ret);
            prefsEditor.commit();
        }

        return ret;
    }

    // These disinct ids are here for the purposes of illustration.
    // In practice, there are great advantages to using distinct ids that
    // are easily associated with user identity, either from server-side
    // sources, or user logins. A common best practice is to maintain a field
    // in your users table to store mixpanel distinct_id, so it is easily
    // accesible for use in attributing cross platform or server side events.
    private String generateDistinctId() {
        final Random random = new Random();
        final byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public void addListenerOnButton() {

        venueGroup = (RadioGroup) findViewById(R.id.radioGroup);
        btnDisplay = (Button) findViewById(R.id.decideButton);

        btnDisplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = venueGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                venueButton = (RadioButton) findViewById(selectedId);

            }

        });

    }

    private String createRequest() {
        String api = "http://api.yelp.com/v2/search/?";
        String venue = venueButton.getText().toString();
        String radius;
        boolean driving = false;
        String location = "San Francisco";
        String resultLimit = "20";
        String request;

        if (driving == false) {
            radius = "1000";
        }
        else {
            radius = "15000";
        }

        request = api + "location=" + location + "&limit=" + resultLimit + "&radius_filter" + radius + "&category_filter=" + venue;

        return request;
    }

    private void sendRequest() {
        String apiRequest = createRequest();
        JsonObjectRequest jsObjRequest;
        jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, apiRequest, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    Log.i("Response",response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Access the RequestQueue through your singleton class.
        Decide.getInstance(this).addToRequestQueue(jsObjRequest);


    }

//    TextView mTxtDisplay = (TextView) findViewById(R.id.text_results);

}
