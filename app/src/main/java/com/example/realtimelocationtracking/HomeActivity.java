package com.example.realtimelocationtracking;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimelocationtracking.Interface.IFirebaseLoadDone;
import com.example.realtimelocationtracking.Interface.IRecyclerItemClickListener;
import com.example.realtimelocationtracking.Model.User;
import com.example.realtimelocationtracking.Service.MyLocationReciever;
import com.example.realtimelocationtracking.Utils.Common;
import com.example.realtimelocationtracking.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter, searchViewAdapter, removeAdapter;
    RecyclerView recycler_friend_list;
    IFirebaseLoadDone firebaseLoadDone;

    MaterialSearchView searchView;

    //Toolbar removefriend;

    DatabaseReference publicLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    /*private void removefriends(final User model){
        final DatabaseReference removefriends1 = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        removefriends1.child(model.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HomeActivity.this, "Successfully removed from friends", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbarhome);
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.search_viewhome);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        final TextView txt_user_logged = headerView.findViewById(R.id.txt_logged_email);
        txt_user_logged.setText(Common.loggedUser.getEmail());

        /*final Button removefriendabc = findViewById(R.id.removefriendabc);
        removefriendabc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (newText != null && !newText.isEmpty()){
                    Query query = FirebaseDatabase.getInstance()
                            .getReference(Common.USER_INFORMATION)
                            .child(Common.loggedUser.getUid())
                            .child(Common.ACCEPT_LIST);

                    FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();

                    searchViewAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options){

                        @NonNull
                        @Override
                        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View itemView = LayoutInflater.from(viewGroup.getContext())
                                    .inflate(R.layout.layout_user, viewGroup, false);
                            return new UserViewHolder(itemView);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {

                            if(!model.getEmail().equals(Common.loggedUser.getEmail()) && model.getEmail().contains(newText)){
                                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                                    @Override
                                    public void onItemClickListener(View view, int position) {
                                        Common.trackingUser = model;
                                        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
                                    }

                                });

                            }
                            else
                                holder.cardView.setVisibility(View.GONE);

                        }
                    };
                    searchViewAdapter.startListening();
                    recycler_friend_list.setAdapter(searchViewAdapter);
                }
                else{
                    recycler_friend_list.setAdapter(searchViewAdapter);
                }

                return true;
            }
        });

        recycler_friend_list = findViewById(R.id.recycler_friend_list);
        recycler_friend_list.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_friend_list.setLayoutManager(layoutManager);
        recycler_friend_list
                .addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager)
                        .getOrientation()));
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        updateLocation();

        firebaseLoadDone = this;

        loadFriendList();
        loadSearchData();

    }

    private void loadSearchData() {
        final List<String> lstUserEmail = new ArrayList<>();

        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot:dataSnapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    lstUserEmail.add(user.getEmail());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void loadFriendList() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                if (model.getEmail().equals(Common.loggedUser.getEmail()))
                    holder.cardView.setVisibility(View.GONE);
                else {
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                    /*holder.removefriendabc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);

                            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                                    .setQuery(query, User.class)
                                    .build();

                            removeAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options){

                                @NonNull
                                @Override
                                public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    return null;
                                }

                                @Override
                                protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull User model1) {
                                    removefriends(model1);
                                    holder.cardView.setVisibility(View.GONE);
                                }
                            };
                        }
                    });*/
                }

                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        Common.trackingUser = model;
                        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_friend_list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        if(adapter != null)
            adapter.startListening();
        if(searchAdapter != null)
            searchAdapter.startListening();
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(adapter != null)
            adapter.stopListening();
        if(searchAdapter != null)
            searchAdapter.stopListening();

        super.onStop();
    }

 /*  private void showAlertDialogRemove(final User model) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.myRequestDialog);
        alertDialog.setTitle("Remove this friend");
        alertDialog.setMessage("do you want remove "+model.getEmail()+" from friend list?");
        alertDialog.setIcon(R.drawable.ic_close_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference acceptList = FirebaseDatabase.getInstance()
                        .getReference(Common.USER_INFORMATION)
                        .child(Common.loggedUser.getUid())
                        .child(Common.ACCEPT_LIST);
                acceptList.orderByKey().equalTo(model.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });

        alertDialog.show();
    }*/

    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(HomeActivity.this, MyLocationReciever.class);
        intent.setAction(MyLocationReciever.ACTION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setFastestInterval(3000);
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startSearch(String search_value) {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST)
                .orderByChild("name")
                .startAt(search_value);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));

                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        Common.trackingUser = model;
                        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recycler_friend_list.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {

        if(searchView.isSearchOpen()){
            searchView.closeSearch();
        }
        else {
            super.onBackPressed();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_find_people) {
            startActivity(new Intent(HomeActivity.this, AllPeopleActivity.class));
        } else if (id == R.id.nav_add_people) {
            startActivity(new Intent(HomeActivity.this, FriendRequestActivity.class));
        } else if (id == R.id.nav_sign_out) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
