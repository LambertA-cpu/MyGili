package com.example.gili;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.gili.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {
    private Button mCleaner,mCustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        mCleaner= (Button) findViewById(R.id.cleaner);
        mCustomer= (Button)findViewById(R.id.customer);

        mCleaner.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, CleanerLoginActivity.class);
                startActivity(intent);
                return;
            }
        });

        mCustomer.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                return;
            }
        });
    }

}
