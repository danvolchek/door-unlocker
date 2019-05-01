package com.danvolchek.doormanager;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    private Handler handler;
    private AES aes;
    private static int Delay = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.lockButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendCommand(DoorAction.Lock);
            }
        });

        findViewById(R.id.unlockButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendCommand(DoorAction.Unlock);
            }
        });

        // TODO: replace with your key and iv
        this.aes = new AES(new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
                new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
        this.handler = new Handler();

        this.handler.postDelayed(new Runnable(){
            public void run(){
                MainActivity.this.GetLockState();
                MainActivity.this.handler.postDelayed(this, MainActivity.Delay);
            }
        }, MainActivity.Delay);
    }

    private void SendCommand(DoorAction action){
        long time = System.currentTimeMillis() / 1000L;

        String plainText = action.name().toLowerCase() + ";" + time;
        Log.i("MSG", "Plaintext: " + plainText);
        try{
            byte[] encrypted = this.aes.encrypt(plainText.getBytes());
            Log.i("MSG", "Encrypted: " + new String(encrypted));
            this.SendToMailbox(Base64.encodeToString(encrypted, Base64.DEFAULT));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to encrypt message", Toast.LENGTH_SHORT).show();
        }
    }

    private void SendToMailbox(String data){
        Log.i("MSG", "Sent " + data);
        // TODO: replace 127.0.0.1 with the ip address of your yun
        StringRequest request = new StringRequest(Request.Method.POST, String.format("http://127.0.0.1/mailbox/%s", data), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Sent request.", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
                Log.e("request-error", error.getMessage());
            }
        });

        this.requestQueue.add(request);
    }

    private void GetLockState(){
        // TODO: replace 127.0.0.1 with the ip address of your yun
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://127.0.0.1/data/get/state",null,   new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int state;
                        try{
                            state = Integer.parseInt(response.getString("value"));
                        } catch (JSONException e){
                            state = 0;
                        }
                        ((TextView)findViewById(R.id.state)).setText(state == 0 ? "Locked" : "Unlocked");
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        this.requestQueue.add(request);
    }
}
