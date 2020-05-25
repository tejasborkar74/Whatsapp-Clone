package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registerationActivity extends AppCompatActivity {

    EditText userEmail,userPassword;
    TextView AlreadyHaveAccount;
    Button CreateAccountButton;
    ProgressDialog progressDialog;
    DatabaseReference rootRef;
    FirebaseAuth userAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Create New Account");

        userEmail=(EditText)findViewById(R.id.emailRegisterEditText);
        userPassword=(EditText)findViewById(R.id.passwordRegisterEditText);
        CreateAccountButton=(Button)findViewById(R.id.registerButton);
        AlreadyHaveAccount=(TextView)findViewById(R.id.AlreadyTextView);
        userAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        rootRef= FirebaseDatabase.getInstance().getReference();


        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLogin();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                RegisterUser();
            }
        });




    }

    private void RegisterUser()
    {
        String Email=userEmail.getText().toString();
        String Password=userPassword.getText().toString();

        if(TextUtils.isEmpty(Email))
        {
            Toast.makeText(registerationActivity.this,"Please enter the Email Address",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(Password))
        {
            Toast.makeText(registerationActivity.this,"Please enter the Password",Toast.LENGTH_LONG).show();
        }
        else
        {
            progressDialog.setTitle("Creating Account");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


        userAuth.createUserWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            String currentUserID=userAuth.getCurrentUser().getUid();
                            rootRef.child("User").child(currentUserID).setValue("");

                            sendUserToMain();
                            Toast.makeText(registerationActivity.this,"Account Created Successfully...",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(registerationActivity.this,"Error: "+ error,Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                        }
                    }
                });
        }
    }

    private void sendUserToMain() {
        Intent toMainInIntent= new Intent(registerationActivity.this,MainActivity.class);
        toMainInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainInIntent);
        finish();
    }

    private void sendUserToLogin()
    {
        Intent toLogInIntent= new Intent(registerationActivity.this,loginActivity.class);
        startActivity(toLogInIntent);
    }
}
