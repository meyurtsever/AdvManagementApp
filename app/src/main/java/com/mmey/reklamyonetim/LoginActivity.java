package com.mmey.reklamyonetim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private Button buttonSignIn;
    private EditText editTextMail;
    private EditText editTextPassword;
    private TextView textViewSignUp;

    //private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private CollectionReference userRef = db.collection("User");

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = database.getReference("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);

        editTextMail = (EditText) findViewById(R.id.editTextMail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        textViewSignUp = (TextView) findViewById(R.id.textViewSignUp);

        getSupportActionBar().setTitle("Giriş Ekranı");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //firebaseAuth = FirebaseAuth.getInstance();
        //if(firebaseAuth.getCurrentUser() != null) {
        //already signed in. activity goster.
        // }
    }

    public void logInUser(View v) {
        final String email = editTextMail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

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
        progressDialog.setMessage("Giriş yapılıyor...");
        progressDialog.show();

        usersRef.orderByChild("userName").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnap : dataSnapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user.getUserPassword().equals(password)) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Başarıyla giriş yapıldı.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                            //    finish();
                        }
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Kullanıcı adı ya da şifre hatalı.", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.dismiss();
                //Toast.makeText(LoginActivity.this, "Şifre hatalı.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }



    /*
        db.collectionGroup("User")
                .whereEqualTo("userName", email)
                .whereEqualTo("userPassword", password)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Kullanıcı adı ya da şifre hatalı.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Başarıyla giriş yapıldı.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        }

                    }
                });
    */
    /*    db.collectionGroup("User").whereEqualTo("userName", email)
                .whereEqualTo("userPassword", password)
               // .orderBy("userName")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressDialog.dismiss();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            User user = documentSnapshot.toObject(User.class);
                            user.setUserID(documentSnapshot.getId());

                            String companyId = user.getUserID();
                            String userName = user.getUserName();
                            String userPassword = user.getUserPassword();

                            if(userName.equals(email) && userPassword.equals(password)) {
                                Toast.makeText(LoginActivity.this, "BULDUN LEN", Toast.LENGTH_SHORT).show();
                            }


                        }

                        Toast.makeText(LoginActivity.this, "Başarıyla giriş yapıldı.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("LoginActivity", e.toString());
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Şifre hatalı?", Toast.LENGTH_SHORT).show();
                    }
                });
    */

     /*   firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            finish();
                            Toast.makeText(LoginActivity.this, "Successfully logged in.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Error while signing in.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d("LoginActivity", e.toString());
                    }
                });
    */



    public void signUpRedirect(View v) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void forgotPassword(View v) {
        startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
    }
}