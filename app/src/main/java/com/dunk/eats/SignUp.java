package com.dunk.eats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.dunk.eats.Common.Common;
import com.dunk.eats.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.edtPhone1)
    MaterialEditText edtPhone1;
    @BindView(R.id.edtName1) MaterialEditText edtName1;
    @BindView(R.id.edtPassword1) MaterialEditText edtPassword1;

    @BindView(R.id.btnSignUp1) Button btnSignUp;

    String current1;

    private DatabaseReference table_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        btnSignUp.setOnClickListener(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
    }

    @Override
    public void onClick(View view) {

        if (view == btnSignUp) {

            final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
            mDialog.setMessage("Signing Up....");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(edtPhone1.getText().toString()).exists()) {
                        Toast.makeText(SignUp.this, "Phone Number already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        mDialog.dismiss();
                        User newuser = new User(edtName1.getText().toString(), edtPassword1.getText().toString());
                        table_user.child(edtPhone1.getText().toString()).setValue(newuser);
                        Toast.makeText(SignUp.this, "Sign Up successfully !", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this,SignIn.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

}
