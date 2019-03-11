package com.stfactory.tutorial5_1gatt_centralandperipheral;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.stfactory.tutorial5_1gatt_centralandperipheral.central.CentralActivity;
import com.stfactory.tutorial5_1gatt_centralandperipheral.peripheral.PeripheralActivity;

public class MainActivity extends AppCompatActivity {


    private Button buttonServer, buttonClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonServer = findViewById(R.id.launch_server_button);
        buttonClient = findViewById(R.id.launch_client_button);


        buttonServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PeripheralActivity.class);
                startActivity(intent);
            }
        });


        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CentralActivity.class);
                startActivity(intent);
            }
        });
    }
}
