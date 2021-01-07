package com.example.helpinghand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        SharedPreferences sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String y=sp.getString("key", "n");
        String token=sp.getString("token", "n");
        editor.commit();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null || !sp.contains("key") || !y.equals("y")){
            Intent intent=new Intent(HomeActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        }else {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(task.isSuccessful()){

                        FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("device_token")
                                .setValue(task.getResult().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                }else {}
                            }
                        });
                    }
                }
            });
        }
    }

    public void loadAssociates(View view) {
        switch (view.getId()){
            case R.id.cookBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Cook");
                startActivity(intent);
                break;
            case R.id.cleanerBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Cleaner");
                startActivity(intent);
                break;
            case R.id.pestControlBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Pest Control");
                startActivity(intent);
                break;
            case R.id.carWashBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Car Wash");
                startActivity(intent);
                break;
            case R.id.repairBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Repairer");
                startActivity(intent);
                break;
            case R.id.driverBT:
                intent=new Intent(HomeActivity.this,WorkersListActivity.class);
                intent.putExtra("type","Driver");
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.home_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profileIT:
                Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.notifIT:
                Intent intent1=new Intent(HomeActivity.this,NotificatiosActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
