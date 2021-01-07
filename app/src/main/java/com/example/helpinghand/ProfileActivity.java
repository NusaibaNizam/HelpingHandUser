package com.example.helpinghand;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    ImageView profileIV;
    TextView nameTV;
    TextView addressTV;
    TextView phoneTV;
    FirebaseDatabase database;
    DatabaseReference accountDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;
    AccountClass accountClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileIV=findViewById(R.id.profileIV);
        nameTV=findViewById(R.id.nameTV);
        addressTV=findViewById(R.id.addressTV);
        phoneTV=findViewById(R.id.phoneTV);
        database=FirebaseDatabase.getInstance();
        if (database == null)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        accountDatabase=database.getReference("accounts");
        accountDatabase.keepSynced(true);
        mAuth=FirebaseAuth.getInstance();

    }

    public void applyForJob(View view) {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[] {Manifest.permission.CAMERA}, 9898);
        }
        Intent intent=new Intent(ProfileActivity.this,JobApplicationActivity.class);
        intent.putExtra("account",accountClass);
        startActivity(intent);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user=currentUser;
        accountDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accountClass=dataSnapshot.getValue(AccountClass.class);
                Picasso.get().load(accountClass.getImage()).resize(1025,850).centerCrop().into(profileIV);
                nameTV.setText(accountClass.getAccountName());
                addressTV.setText(accountClass.getAddress());
                phoneTV.setText(accountClass.getPhoneNumber());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.profile_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.homeIT:
                Intent intent=new Intent(ProfileActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.notifIT:
                Intent intent1=new Intent(ProfileActivity.this,NotificatiosActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void update(View view) {
        Intent intent =new Intent(ProfileActivity.this,AccountActivity.class);
        intent.putExtra("dont","dont");
        startActivity(intent);
    }
}
