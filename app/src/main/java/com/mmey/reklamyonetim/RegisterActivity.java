package com.mmey.reklamyonetim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button buttonRegister;
    private EditText editTextMail;
    private EditText editTextPassword;
    private TextView textViewSignIn;

    //private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private CollectionReference userRef = db.collection("User");
    //private FirebaseAuth firebaseAuth;

    private DatabaseReference databaseUser;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //firebaseAuth = FirebaseAuth.getInstance();

        databaseUser = FirebaseDatabase.getInstance().getReference("User");
        progressDialog = new ProgressDialog(this);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        editTextMail = (EditText) findViewById(R.id.editTextMail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        getSupportActionBar().setTitle("Kayıt Ekranı");
    }

    public void registerUser(View v) {
        String email = editTextMail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(this, "Email boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Şifre boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show the status bar.
        progressDialog.setMessage("Kayıt işlemi sürüyor...");
        progressDialog.show();

        String id = databaseUser.push().getKey(); // unique id

        User user = new User(id, email, password);

        databaseUser.child(id).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Kayıt işlemi gerçekleşti.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                // finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Kayıt işleminde hata.", Toast.LENGTH_SHORT).show();
                Log.d("RegisterActivityExc", e.toString());
            }
        });
        // CLOUD FIRESTORE
    /*    userRef.add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Kayıt işlemi gerçekleşti.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Kayıt işleminde hata.", Toast.LENGTH_SHORT).show();
                        Log.d("RegisterActivity", e.toString());
                    }
                });
    */
    /*    firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.hide();
                            // user is successfully registered and logged in. show the next layout.
                            Toast.makeText(RegisterActivity.this, "Registration success.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        } else {
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Error while registering user.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("RegisterActivity", e.toString());
                    }
                });
    */

    }

    public void signInRedirect(View v) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        // finish();
    }

    public void forgotPassword(View v) {
        startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
        // finish();
    }
}