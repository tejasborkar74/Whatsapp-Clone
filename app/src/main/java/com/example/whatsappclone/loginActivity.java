package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginActivity extends AppCompatActivity {

    EditText userEmail,userPassword;
    TextView ForgetPasswordLink;
    Button loginButton,signInButton;
    FirebaseAuth userAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton=(Button)findViewById(R.id.loginButton);
        signInButton=(Button)findViewById(R.id.SignupButton);
        userEmail=(EditText)findViewById(R.id.emailEditText);
        userPassword=(EditText)findViewById(R.id.passwordEditText);
        ForgetPasswordLink=(TextView)findViewById(R.id.ForgePasswordtTextView);
        userAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegister();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                loginTheUser();
            }
        });


    }

    private void loginTheUser()
    {
        String Email=userEmail.getText().toString();
        String Password=userPassword.getText().toString();
        if(TextUtils.isEmpty(Email) && TextUtils.isEmpty(Password))
        {
            Toast.makeText(loginActivity.this,"Please enter the Email Address and Password",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(Email))
        {
            Toast.makeText(loginActivity.this,"Please enter the Email Address",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(Password))
        {
            Toast.makeText(loginActivity.this,"Please enter the Password",Toast.LENGTH_LONG).show();
        }
        else
        {
            progressDialog.setTitle("Logging In");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            userAuth.signInWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMain();
                                progressDialog.dismiss();
                                Toast.makeText(loginActivity.this,"Logged in Successfully...",Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                String error=task.getException().toString();
                                Toast.makeText(loginActivity.this,"Error: "+ error,Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }


    private void sendUserToMain() {
        Intent toMainInIntent= new Intent(loginActivity.this,MainActivity.class);
        toMainInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainInIntent);
        finish();
    }
    private void sendUserToRegister()
    {
        Intent registerIntent= new Intent(getApplicationContext(),registerationActivity.class);
        startActivity(registerIntent);
    }
}
