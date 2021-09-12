package pl.polsl.mqttclientapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ConnectionSetupDialogFragment.SetupDialogListener {

    private MqttAndroidClient mqttClient;
    private final View.OnClickListener connectButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConnectionSetupDialogFragment dialogFragment = new ConnectionSetupDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "connect");
        }
    };
    private final View.OnClickListener disconnectButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                mqttClient.disconnect();
                Button btnConnect = findViewById(R.id.btnConnect);
                btnConnect.setOnClickListener(disconnectButtonClickListener);
                btnConnect.setText("Connect");
            } catch (MqttException e) {
                e.printStackTrace();
                Log.wtf("MQTT", "disconnect failed somehow lmao");
            }
        }
    };
    private HashMap<String, String> messages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnConnect).setOnClickListener(connectButtonClickListener);
    }

    @Override
    public void onDialogPositiveClick(ConnectionSetupDialogFragment dialog) {
        mqttClient = new MqttAndroidClient(getBaseContext(), dialog.getBrokerUri(), "");
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), String.format("Connection lost - %s", cause.getMessage()), Toast.LENGTH_LONG).show();
                TextView txtvStatus = findViewById(R.id.txtvStatus);
                txtvStatus.setText("Connection lost");
                Log.w("MQTT", String.format("Connection lost - %s", cause.getMessage()), cause);
                Button btnConnect = findViewById(R.id.btnConnect);
                btnConnect.setText("Connect");
                btnConnect.setOnClickListener(connectButtonClickListener);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                messages.put(topic, new String(message.getPayload(), Charset.defaultCharset()));
                refreshMessageDisplay();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //not implemented
            }
        });
        try {
            mqttClient.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), String.format("Connection failed - %s", e.getMessage()), Toast.LENGTH_LONG);
            TextView txtvStatus = findViewById(R.id.txtvStatus);
            txtvStatus.setText("Connection failed");
        }
    }

    @Override
    public void onDialogNegativeClick(ConnectionSetupDialogFragment dialog) {
        //do absolutely nothing
    }

    private void refreshMessageDisplay() {
        TextView textView = findViewById(R.id.txtvMessages);
        textView.setText("");
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> message : messages.entrySet()) {
            builder.append(message.getKey())
                    .append(":\t")
                    .append(message.getValue());
        }
        textView.setText(builder.toString());
    }
}