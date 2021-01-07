package com.example.helpinghand;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcceptedWorkerProfileActivity extends AppCompatActivity {
    JobClass job;
    CircleImageView profileIV;
    TextView nameTV;
    TextView addressTV;
    TextView preferredLocationTV;
    TextView workTypeTV;
    TextView genderTV;
    TextView salaryTV;
    TextView phoneTV;
    private static final int CALL_REQUEST_CODE=7893;
    FirebaseDatabase database;
    ProgressDialog progress;
    DatabaseReference acceptedJobDatabase;
    DatabaseReference notificationDatabase;
    DatabaseReference appNotificationDatabase;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_worker_profile);
        Intent intent=getIntent();
        job= (JobClass) intent.getSerializableExtra("profile");
        profileIV=findViewById(R.id.profileIV);
        nameTV=findViewById(R.id.nameTV);
        addressTV=findViewById(R.id.addressTV);
        preferredLocationTV=findViewById(R.id.preferredAddressTV);
        workTypeTV=findViewById(R.id.jobTV);
        genderTV=findViewById(R.id.genderTV);
        salaryTV=findViewById(R.id.expectedSalaryTV);
        phoneTV=findViewById(R.id.phoneTV);
        storage= FirebaseStorage.getInstance();
        database= FirebaseDatabase.getInstance();
        acceptedJobDatabase=database.getReference("accepted Jobs").child(job.getWorkType()).child(job.getId());
        notificationDatabase=database.getReference("Notifications").child(job.getId());
        appNotificationDatabase=database.getReference("appNotifications").child(job.getId());
        progress=new ProgressDialog(this);
        progress.setTitle("Helping Hand");
        progress.setMessage("Wait...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);




        Picasso.get().load(job.getProfileImage()).into(profileIV);
        nameTV.setText(job.getName());
        addressTV.setText("Address "+job.getAddress());
        preferredLocationTV.setText("Preferred Location "+job.getPreferredAddress());
        workTypeTV.setText(job.getWorkType());
        genderTV.setText(job.getGender());
        salaryTV.setText(job.getExpectedSalary()+" BDT");
        phoneTV.setText(job.getPhone());
    }

    public void call(View view) {
        if(ContextCompat.checkSelfPermission(AcceptedWorkerProfileActivity.this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AcceptedWorkerProfileActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE},CALL_REQUEST_CODE);
        }else {
            String dial="tel:"+job.getPhone();
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CALL_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                String dial="tel:"+job.getPhone();
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Hire(View view) {
        AppNotification appNotification;
        String appKey=appNotificationDatabase.push().getKey();
        appNotification=new AppNotification(user.getUid(),job.getId()," Wants to Hire You For "+job.getWorkType(),"Hire",appKey,job.workType);
        appNotificationDatabase.child(appKey).setValue(appNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AcceptedWorkerProfileActivity.this,"Request Sent",Toast.LENGTH_LONG).show();
                    Notification notification=new Notification(job.getId(),"You Have A Hire Request");
                    String notifKey=notificationDatabase.push().getKey();
                    notificationDatabase.child(notifKey).setValue(notification);
                } else {
                    Toast.makeText(AcceptedWorkerProfileActivity.this,
                            "Request Not Sent"+"\n"+task.getException().getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
    }
}