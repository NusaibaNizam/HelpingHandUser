package com.example.helpinghand;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AccountProfileActivity extends AppCompatActivity {
    private static final int CALL_REQUEST_CODE = 7893;
    ImageView profileIV;
    TextView nameTV;
    TextView addressTV;
    TextView phoneTV;
    FirebaseDatabase database;
    DatabaseReference appNotificationDatabase;
    DatabaseReference adminNotificationDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;
    AccountClass accountClass;
    AppNotification notification;
    private DatabaseReference acceptedJobs;
    JobClass job;
    private DatabaseReference notificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_profile);
        profileIV=findViewById(R.id.profileIV);
        nameTV=findViewById(R.id.nameTV);
        addressTV=findViewById(R.id.addressTV);
        phoneTV=findViewById(R.id.phoneTV);
        database=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        Intent intent=getIntent();
        notification= (AppNotification) intent.getSerializableExtra("notif");
        accountClass= (AccountClass) intent.getSerializableExtra("account");
        Picasso.get().load(accountClass.getImage()).resize(1025,850).centerCrop().into(profileIV);
        nameTV.setText(accountClass.getAccountName());
        addressTV.setText(accountClass.getAddress());
        phoneTV.setText(accountClass.getPhoneNumber());
        appNotificationDatabase=database.getReference("appNotifications");
        adminNotificationDatabase=database.getReference("adminNotifications");
        notificationDatabase=database.getReference("Notifications").child(accountClass.getId());

    }

    public void accept(View view) {
        final String key=appNotificationDatabase.child(accountClass.getId()).push().getKey();
        final AppNotification newNotification= new AppNotification(notification.getToID(),
                accountClass.getId()," Has Accepted Your Request To Work For You As "+notification.getWorkType(),
                "Accept",key,notification.getWorkType());
        AppNotification adminNotification= new AppNotification(notification.getToID(),
                accountClass.getId()," Has Accepted  Request To Work For " +accountClass.getAccountName()+"As "+notification.getWorkType(),
                "Accept",key,notification.getWorkType());

        adminNotificationDatabase.child(key).setValue(adminNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    appNotificationDatabase.child(accountClass.getId()).child(key).setValue(newNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                appNotificationDatabase.child(user.getUid()).child(notification.getNotifID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            Notification notification=new Notification(accountClass.getId(),"A Worker Has Accepted Your Request");
                                            String notifKey=notificationDatabase.push().getKey();
                                            notificationDatabase.child(notifKey).setValue(notification);
                                            Toast.makeText(AccountProfileActivity.this,"Accepted",Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(AccountProfileActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });

    }

    public void delete(View view) {
        String key=appNotificationDatabase.child(accountClass.getId()).push().getKey();
        AppNotification newNotification= new AppNotification(notification.getToID(),
                accountClass.getId()," Has Rejected Your Request To Work For You As "+notification.getWorkType(),
                "Accept",key,notification.getWorkType());
        appNotificationDatabase.child(accountClass.getId()).child(key).setValue(newNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    appNotificationDatabase.child(user.getUid()).child(notification.getNotifID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Notification notification=new Notification(accountClass.getId(),"A Worker Has Accepted Your Request");
                                String notifKey=notificationDatabase.push().getKey();
                                notificationDatabase.child(notifKey).setValue(notification);
                                Toast.makeText(AccountProfileActivity.this,"Denied",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(AccountProfileActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
    }

    public void call(View view) {
        if(ContextCompat.checkSelfPermission(AccountProfileActivity.this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AccountProfileActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE},CALL_REQUEST_CODE);
        }else {
            String dial="tel:"+accountClass.getPhoneNumber();
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CALL_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                String dial="tel:"+accountClass.getPhoneNumber();
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
