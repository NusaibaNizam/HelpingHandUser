package com.example.helpinghand;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JobApplicationActivity extends AppCompatActivity {
    Spinner genderSp;
    Spinner jobSp;
    EditText preferredLocationET;
    EditText expectedSalaryET;
    ProgressBar progressBar;
    TextView errorTV;
    String preferredLocation,salary;
    String gender;
    String job;
    AccountClass accountClass;
    Button applyBT;
    boolean front,back;
    int which;
    Button frontBT;
    Button backBT;
    String frontImage,backImage;
    private static final int CAMERA_REQUEST_CODE=3334;
    FirebaseDatabase database;
    DatabaseReference applyJobDatabase;
    StorageReference NIDStorage;
    private String captureImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_application);
        genderSp=findViewById(R.id.genderSP);
        jobSp=findViewById(R.id.jobSP);
        applyBT=findViewById(R.id.applyBT);
        progressBar=findViewById(R.id.progressBar);
        errorTV=findViewById(R.id.errorTV);
        preferredLocationET=findViewById(R.id.preferredLocationET);
        expectedSalaryET=findViewById(R.id.expectedSalaryET);
        frontBT=findViewById(R.id.frontBT);
        backBT=findViewById(R.id.backBT);
        database=FirebaseDatabase.getInstance();
        NIDStorage= FirebaseStorage.getInstance().getReference();
        applyJobDatabase=database.getReference("appliedJobs");
        front=false;
        back=false;

        applyBT.setEnabled(false);
        ArrayAdapter<CharSequence> arrayAdapter=ArrayAdapter.createFromResource(this,R.array.gender,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSp.setAdapter(arrayAdapter);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.jobs,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobSp.setAdapter(adapter);
        Intent intent=getIntent();
        accountClass= (AccountClass) intent.getSerializableExtra("account");


        genderSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gender=parent.getItemAtPosition(0).toString();
            }
        });


        jobSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                job=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                job=parent.getItemAtPosition(0).toString();
            }
        });
    }

    public void uploadNID(View view) throws IOException {

        if (view.getId() == R.id.frontBT)
            which = 1;
        else if (view.getId() == R.id.backBT)
            which = 2;
        if (ContextCompat.checkSelfPermission(JobApplicationActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(JobApplicationActivity.this, new String[] {Manifest.permission.CAMERA}, 9898);
            return;
        }else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.helpinghand.provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        captureImagePath = image.getAbsolutePath();
        return image;
    }

    public void apply(View view) {
        UIUtil.hideKeyboard(JobApplicationActivity.this);
        preferredLocation=preferredLocationET.getText().toString();
        salary=expectedSalaryET.getText().toString();
        if(TextUtils.isEmpty(preferredLocation)||TextUtils.isEmpty(salary)){
            errorTV.setText("");

        }else {

            progressBar.setVisibility(View.VISIBLE);
            JobClass jobClass=new JobClass(accountClass.getId(),accountClass.getFullName(),accountClass.getAddress(),preferredLocation
            ,accountClass.getPhoneNumber(),gender,job,accountClass.getImage(),frontImage,backImage,salary);
            applyJobDatabase.child(accountClass.getId()).setValue(jobClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(JobApplicationActivity.this,"Applied",Toast.LENGTH_LONG).show();
                        Intent intent =new Intent(JobApplicationActivity.this,ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        errorTV.setText(task.getException().getLocalizedMessage());
                        progressBar.setVisibility(View.GONE);
                        errorTV.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            progressBar.setVisibility(View.VISIBLE);
            Uri uri = Uri.fromFile(new File(captureImagePath));
            Date date=new Date();
            frontBT.setEnabled(false);
            backBT.setEnabled(false);
            Timestamp timestamp=new Timestamp(date.getTime());
            String name;
            if(which==1){
                name=accountClass.getId()+"_front_NID";
            }else {
                name=accountClass.getId()+"_back_NID";

            }
            final StorageReference filePath=NIDStorage.child("NID Photos").child(name);
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            if(which==1) {
                                frontImage=String.valueOf(downloadUrl);
                                front=true;
                            }else if(which==2){
                                backImage=String.valueOf(downloadUrl);
                                back=true;
                            }
                            progressBar.setVisibility(View.GONE);
                            frontBT.setEnabled(true);
                            backBT.setEnabled(true);
                            if(front&&back) {
                                applyBT.setEnabled(true);
                            }
                            Toast.makeText(JobApplicationActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();

                        }

                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorTV.setText(e.getLocalizedMessage());
                    progressBar.setVisibility(View.GONE);
                    errorTV.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(JobApplicationActivity.this);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==9898){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    }catch (IOException e){}
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.example.helpinghand.provider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                    }
                }
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
