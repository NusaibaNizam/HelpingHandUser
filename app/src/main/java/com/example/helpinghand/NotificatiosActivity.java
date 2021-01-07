package com.example.helpinghand;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.ArrayList;

public class NotificatiosActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private FirebaseDatabase database;
    RecyclerView jobApplicantsRV;
    ArrayList<AppNotification> jobs;
    FirebaseRecyclerOptions<AppNotification> jobOptions;
    FirebaseRecyclerAdapter<AppNotification, NotificationHolder> jobAdapter;
    private DatabaseReference appNotificationDatabase;
    private DatabaseReference accountDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificatios);
        database= FirebaseDatabase.getInstance();
        if (database == null)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Intent intent=getIntent();
        String type=intent.getStringExtra("type");
        mAuth= FirebaseAuth.getInstance();
        accountDatabase = database.getReference("accounts");
        jobApplicantsRV=findViewById(R.id.jobApplicantsRV);


    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseUser user;
        user=mAuth.getCurrentUser();
        accountDatabase.keepSynced(true);
        appNotificationDatabase=database.getReference("appNotifications").child(user.getUid());

        appNotificationDatabase.keepSynced(true);
        jobs=new ArrayList<>();
        jobApplicantsRV.setHasFixedSize(true);
        jobApplicantsRV.setLayoutManager(new LinearLayoutManager(this));
        jobOptions=new FirebaseRecyclerOptions.Builder<AppNotification>().setQuery(appNotificationDatabase,AppNotification.class).build();
        jobAdapter = new FirebaseRecyclerAdapter<AppNotification, NotificationHolder>(jobOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationHolder holder, int position, @NonNull final AppNotification model) {
                if(model.getFromID().equals("admin")){
                    holder.notifTV.setText(model.getText());
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(NotificatiosActivity.this);
                            builder1.setMessage("Remove Notification?");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            delete(model.getNotifID());
                                            dialog.cancel();
                                        }
                                    });

                            builder1.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();

                            return true;
                        }
                    });

                } else {
                    accountDatabase.child(model.getFromID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final AccountClass accountClass = dataSnapshot.getValue(AccountClass.class);
                            holder.notifTV.setText(accountClass.getFullName() + model.getText());


                            if (model.getType().equals("Hire")) {

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(NotificatiosActivity.this, AccountProfileActivity.class);
                                        intent.putExtra("account", accountClass);
                                        intent.putExtra("notif", model);
                                        startActivity(intent);
                                    }
                                });
                            } else {

                                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(NotificatiosActivity.this);
                                        builder1.setMessage("Remove Notification?");
                                        builder1.setCancelable(true);

                                        builder1.setPositiveButton(
                                                "Yes",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        delete(model.getNotifID());
                                                        dialog.cancel();
                                                    }
                                                });

                                        builder1.setNegativeButton(
                                                "No",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                        AlertDialog alert11 = builder1.create();
                                        alert11.show();

                                        return true;
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @NonNull
            @Override
            public NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new NotificationHolder(LayoutInflater.from(NotificatiosActivity.this)
                        .inflate(R.layout.row_notification,viewGroup,false));
            }
        };

        jobApplicantsRV.setAdapter(jobAdapter);
        jobAdapter.startListening();
    }

    private void delete(String notifID) {
        appNotificationDatabase.child(notifID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(NotificatiosActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(NotificatiosActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        jobAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.notification_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.homeIT:
                Intent intent=new Intent(NotificatiosActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.profileIT:
                Intent intent1=new Intent(NotificatiosActivity.this,ProfileActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

