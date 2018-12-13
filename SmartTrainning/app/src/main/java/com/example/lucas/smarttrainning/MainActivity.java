package com.example.lucas.smarttrainning;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

//    private MqttAndroidClient client;
//    private String TAG = "MainActivity";
//    private PahoMqttClient pahoMqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // pahoMqttClient = new PahoMqttClient();

        Button btnInfo = findViewById(R.id.btIniciar);
        ImageButton btnSensor = findViewById(R.id.btSensores);

        //client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);

        btnSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MostraAcel.class);
                startActivity(i);
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(i);

//                String topic = Constants.TOPIC_ATT;
//
//                if(!topic.isEmpty()){
//                try {
//                    pahoMqttClient.subscribe(client, topic, 1);
//                    Toast.makeText(MainActivity.this, topic, Toast.LENGTH_SHORT).show();
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
//
//                }
            }
        });

//        Intent intent = new Intent(MainActivity.this, MqttMessageService.class);
//        startService(intent);

    }
}
