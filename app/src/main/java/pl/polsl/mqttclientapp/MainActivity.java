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
import java.util.Collections;
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
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Button btnConnect = findViewById(R.id.btnConnect);
            TextView txtvStatus = findViewById(R.id.txtvStatus);
            txtvStatus.setText("Disconnected");
            btnConnect.setOnClickListener(connectButtonClickListener);
            btnConnect.setText("Connect");
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
                TextView txtvStatus = findViewById(R.id.txtvStatus);
                txtvStatus.setText("Connection lost");
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
                    try {
                        mqttClient.subscribe((String[])dialog.getTopics().toArray(),
                                Collections.nCopies(dialog.getTopics().size(), 1).stream().mapToInt(i->i).toArray()); //create an int array of size the same as the topics list, filled with 1s
                        Button btnConnect = findViewById(R.id.btnConnect);
                        TextView txtvStatus = findViewById(R.id.txtvStatus);
                        btnConnect.setOnClickListener(disconnectButtonClickListener);
                        btnConnect.setText("Disconnect");
                        txtvStatus.setText(String.format("Connected to broker %s", dialog.getBrokerUri()));
                    } catch (MqttException e) {
                        e.printStackTrace();
                        ((TextView)findViewById(R.id.txtvStatus)).setText(String.format("Subscription error - %s", e.getMessage()));
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    ((TextView)findViewById(R.id.txtvStatus)).setText(String.format("Connection failed - %s", exception.getMessage()));
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
                    .append(message.getValue())
                    .append("\n");
        }
        textView.setText(builder.toString());
    }
}