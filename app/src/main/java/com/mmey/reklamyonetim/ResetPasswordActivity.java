package com.mmey.reklamyonetim;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetPasswordActivity extends AppCompatActivity {

    private Button buttonReset;
    private EditText userName;
    private EditText oldPassword;
    private EditText newPassword;
    private EditText new2Password;

    //private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private CollectionReference userRef = db.collection("User");

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = database.getReference("User");

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        buttonReset = (Button) findViewById(R.id.buttonChangePass);
        userName = (EditText) findViewById(R.id.editTextMail);
        oldPassword = (EditText) findViewById(R.id.editTextOldPassword);
        newPassword = (EditText) findViewById(R.id.editTextNewPassword);
        new2Password = (EditText) findViewById(R.id.editTextNew2Password);

        progressDialog = new ProgressDialog(this);

        getSupportActionBar().setTitle("Şifre Sıfırlama Ekranı");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void changePassword(View v) {
        String user = userName.getText().toString().trim();
        final String oldPass = oldPassword.getText().toString().trim();
        final String newPass = newPassword.getText().toString().trim();
        String new2Pass = new2Password.getText().toString().trim();

        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(new2Pass)) {
            //tum alanlar dolmali.
            Toast.makeText(this, "Tüm alanlar doldurulmalıdır.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(new2Pass)) {
            AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this).create();
            alertDialog.setTitle("Yeni şifre");
            alertDialog.setMessage("Yeni şifreler birbiriyle uyuşmuyor.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { } });
            alertDialog.show();
            return;
        }

        progressDialog.setMessage("Şifre değiştirme işlemi sürüyor...");
        progressDialog.show();

        usersRef.orderByChild("userName").equalTo(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnap : dataSnapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user.getUserPassword().equals(oldPass)) {
                            updatePassword(user.getUserID(), user.getUserName(), newPass);
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(ResetPasswordActivity.this, "Kullanıcı adı ya da şifre doğru değil.", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        /*
        db.collectionGroup("User")
                .whereEqualTo("userName", user)
                .whereEqualTo("userPassword", oldPass)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()) {
                            progressDialog.dismiss();
                            Toast.makeText(ResetPasswordActivity.this, "Eski kullanıcı adı ya da şifre hatalı.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(ResetPasswordActivity.this, "Başarıyla giriş yapıldı.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        }

                    }
                });
        */
    }

    private void updatePassword(String userId, String userName, String newPassword) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User").child(userId);

        User user = new User(userId, userName, newPassword);
        databaseReference.setValue(user);
        Toast.makeText(this, "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show();
    }
}


