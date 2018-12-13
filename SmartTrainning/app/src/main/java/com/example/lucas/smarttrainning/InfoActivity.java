package com.example.lucas.smarttrainning;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;

    final String serverUri = "tcp://iot.eclipse.org:1883";
    String clientId = "ExampleAndroidClient";
    final String subscriptionTopic = "mov";
    final String publishTopic = "train";
    final String stopTrain = "stopTrain";
    final String startTrain = "startTrain";

    private ListView listTypes;
    private TextView numAttack;
    private TextView numBlock;
    private TextView numTotal;
    private Button finalTreino;
    private int attack = 0;
    private int block = 0;
    private int total = 0;
    private ArrayList<String> msgMQTT = new ArrayList<String>();
    private ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        listTypes = (ListView) findViewById(R.id.listTypes);
        numAttack = findViewById(R.id.numAttack);
        numBlock = findViewById(R.id.numBlock);
        numTotal = findViewById(R.id.numeroTotal);
        finalTreino = findViewById(R.id.finalizaTreino);

        finalTreino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    publishMessage(stopTrain);
                    unSubscribe(mqttAndroidClient,subscriptionTopic);
                    disconnect(mqttAndroidClient);
                    finish();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgMQTT);
        listTypes.setAdapter(adapter);
        numBlock.setText(String.valueOf(block));
        numAttack.setText(String.valueOf(attack));
        numTotal.setText(String.valueOf(total));

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    //addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    publishMessage(startTrain);
                    subscribeToTopic();
                } else {
                    //addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                // addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast toast= Toast.makeText(getApplicationContext(),  message.toString().toUpperCase(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 0);
                toast.show();

                if(message.toString().equals("ataque")){
                    attack++;
                    total++;
                    msgMQTT.add(0,"O atleta realizou um Ataque");
                    listTypes.setAdapter(adapter);
                    numAttack.setText(String.valueOf(attack));
                    numTotal.setText(String.valueOf(total));
                }

                if(message.toString().equals("bloqueio")){
                    block++;
                    total++;
                    msgMQTT.add(0,"O atleta realizou um Bloqueio");
                    listTypes.setAdapter(adapter);
                    numBlock.setText(String.valueOf(block));
                    numTotal.setText(String.valueOf(total));

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);


        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    publishMessage(startTrain);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to connect to: " + serverUri);
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                }

            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void publishMessage(String publishMessage){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            //addToHistory("Message Published");
            if(!mqttAndroidClient.isConnected()){
                //addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }


    }

    public void unSubscribe(@NonNull MqttAndroidClient client,
                            @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d("Main", "UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e("Main", "UnSubscribe Failed " + topic);
            }
        });
    }

    public void disconnect(@NonNull MqttAndroidClient client)
            throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d("Main", "Successfully disconnected");
            }
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d("Main", "Failed to disconnected " + throwable.toString());
            }
        });
    }

}


// Type 0 = Block
// Type 1 = Atack