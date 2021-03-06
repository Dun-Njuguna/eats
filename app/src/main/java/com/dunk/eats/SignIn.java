package com.dunk.eats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.rey.material.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.edtPhone) MaterialEditText edtPhone;
    @BindView(R.id.edtPassword) MaterialEditText edtPassword;
    @BindView(R.id.btnSignin) Button btnSignIn;
    @BindView(R.id.chkRemember) CheckBox chkRemember;

    private DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        btnSignIn.setOnClickListener(this);

        //init paper
        Paper.init(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

    }

    @Override
    public void onClick(View view) {

        if (view == btnSignIn) {

            if (Common.isConnectedInternet(this)) {

                //save user and password to memmory
                if (chkRemember.isChecked()){
                    Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                    Paper.book().write(Common.PSD_KEY,edtPassword.getText().toString());

                }



                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);

                mDialog.setMessage("Loging in....");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                            mDialog.dismiss();
                            User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            user.setPhone(edtPhone.getText().toString()); //set phone

                            if (user.getPassword().equals(edtPassword.getText().toString())) {
                                {
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Common.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();
                                }
                            } else {

                                Toast.makeText(SignIn.this, "Incorrect password", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this, "User does not exist!", Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else{
                Toast.makeText(this, "Check Internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }
}
