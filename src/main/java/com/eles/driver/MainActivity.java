package com.eles.driver;

import android.content.Intent;
import android.os.Bundle;



import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText userName,password;
    private Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName=findViewById(R.id.username);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login_btn);
        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String name=userName.getText().toString().trim();
                String pass=password.getText().toString().trim();
                if(name.equals("Admin")&&pass.equals("admin123")){
                    Toast.makeText(MainActivity.this, "Login Sucessfully!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Navigation.class));
                }
                else{
                    Toast.makeText(MainActivity.this,"Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}