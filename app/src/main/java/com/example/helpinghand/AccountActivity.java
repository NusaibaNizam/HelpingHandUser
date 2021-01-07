package com.example.helpinghand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.sql.Timestamp;
import java.util.Date;

public class AccountActivity extends AppCompatActivity {

    private String phoneNumber;
    EditText nameET;
    EditText addressET;
    EditText accountNameET;
    TextView errorTV;
    Button imageBT;
    ProgressBar progressBar;
    FirebaseDatabase database;
    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference accountDatabase;
    StorageReference imageStorage;
    Button accountBT;
    private static final int GALLERY_INTENT=2;
    String image;
    private String key;
    private Intent intentCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accout);
        Intent intent=getIntent();
        phoneNumber=intent.getStringExtra("phone");
        nameET=findViewById(R.id.nameET);
        addressET=findViewById(R.id.addressET);
        accountNameET=findViewById(R.id.accountNameET);
        errorTV=findViewById(R.id.errorTV);
        imageBT=findViewById(R.id.imageBT);
        progressBar=findViewById(R.id.progressBar);
        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        accountDatabase = database.getReference("accounts");
        imageStorage= FirebaseStorage.getInstance().getReference();
        accountBT=findViewById(R.id.accountBT);
        accountBT.setEnabled(false);

    }

    public void createAccount(View view) {
        UIUtil.hideKeyboard(AccountActivity.this);
        String name=nameET.getText().toString();
        String address=addressET.getText().toString();
        String accountName=accountNameET.getText().toString();
        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(address)||TextUtils.isEmpty(accountName)||image.equals(null)){
            errorTV.setText("Complete All the Fields");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            AccountClass accountClass=new AccountClass(user.getUid(),name,address,accountName,phoneNumber,image);
            accountDatabase.child(user.getUid()).setValue(accountClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(AccountActivity.this,"Account Created",Toast.LENGTH_LONG).show();
                        SharedPreferences sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("key", "y");
                        editor.commit();
                        Intent intent =new Intent(AccountActivity.this,HomeActivity.class);
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
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user=currentUser;
        final SharedPreferences s = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = s.edit();
        intentCheck=getIntent();
        key=s.getString("key", "n");
        editor.commit();
        if(intentCheck.getStringExtra("dont")==null) {
            accountDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    AccountClass accountClass = dataSnapshot.getValue(AccountClass.class);
                    if (accountClass != null && (!s.contains("key") || !key.equals("y"))) {

                        Toast.makeText(AccountActivity.this, "Account Found", Toast.LENGTH_LONG).show();
                        SharedPreferences sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("key", "y");
                        editor.commit();
                        Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AccountActivity.this, "Error Trying To Find Old Existence Of Account" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    void selectImage(){
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_INTENT && resultCode== RESULT_OK){
            imageBT.setEnabled(false);
            final Uri uri=data.getData();
            Date date=new Date();
            Timestamp timestamp=new Timestamp(date.getTime());
            final StorageReference filePath=imageStorage.child("Profile Photos").child(user.getUid()+"_profile_photo");
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            image= String.valueOf(downloadUrl);
                            progressBar.setVisibility(View.GONE);
                            imageBT.setEnabled(true);
                            accountBT.setEnabled(true);
                            Toast.makeText(AccountActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();

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

    public void selectImage(View view) {
        selectImage();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(AccountActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
