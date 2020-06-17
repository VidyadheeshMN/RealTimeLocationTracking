package com.example.realtimelocationtracking;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimelocationtracking.Interface.IFirebaseLoadDone;
import com.example.realtimelocationtracking.Interface.IRecyclerItemClickListener;
import com.example.realtimelocationtracking.Model.MyResponse;
import com.example.realtimelocationtracking.Model.Request;
import com.example.realtimelocationtracking.Model.User;
import com.example.realtimelocationtracking.Remote.IFCMService;
import com.example.realtimelocationtracking.Utils.Common;
import com.example.realtimelocationtracking.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone{


    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter, searchViewAdapter, removeAdapter;
    RecyclerView recycler_all_user;
    IFirebaseLoadDone firebaseLoadDone;

    MaterialSearchView searchView;

    //Toolbar removefriend;

    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        ifcmService = Common.getFCMService();

        searchView = findViewById(R.id.search_view);

        recycler_all_user = findViewById(R.id.recylcer_all_people);
        recycler_all_user.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        firebaseLoadDone = this;

        loadUserList();
        loadSearchData();

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                loadUserList();
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
                    Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);

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
                            if(model.getEmail().contains(newText)){
                                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                                    @Override
                                    public void onItemClickListener(View view, int position) {
                                        showDialogRequest(model);
                                    }

                                });

                            }
                            else
                                holder.cardView.setVisibility(View.GONE);

                        }
                    };
                    searchViewAdapter.startListening();
                    recycler_all_user.setAdapter(searchViewAdapter);
                }
                else{
                    //searchViewAdapter.startListening();
                    recycler_all_user.setAdapter(searchViewAdapter);
                }

                return true;
            }
        });
    }

    private void loadSearchData() {
        final List<String> lstUserEmail = new ArrayList<>();

        DatabaseReference userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
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

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                if(model.getEmail().equals(Common.loggedUser.getEmail())) {
                    //holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append("(me)"));
                    //holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                    holder.cardView.setVisibility(View.GONE);
                }
                else
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        showDialogRequest(model);
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
        recycler_all_user.setAdapter(adapter);

    }

    private void showDialogRequest(final User model) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.myRequestDialog);
        alertDialog.setTitle("Send Friend Request");
        alertDialog.setMessage("do you want send friend request to "+model.getEmail()+" ?");
        alertDialog.setIcon(R.drawable.ic_account_circle_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
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
                                if(dataSnapshot.getValue() == null)
                                    sendFriendRequest(model);
                                else
                                    Toast.makeText(AllPeopleActivity.this, "You and "+model.getEmail()+" are already friends", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });

        alertDialog.show();
    }

    private void sendFriendRequest(final User model) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS);

        tokens.orderByKey().equalTo(model.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                            Toast.makeText(AllPeopleActivity.this, "Token Error", Toast.LENGTH_SHORT).show();
                        else {
                            Request request = new Request();

                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put(Common.FROM_UID, Common.loggedUser.getUid());
                            dataSend.put(Common.FROM_NAME, Common.loggedUser.getEmail());
                            dataSend.put(Common.TO_UID, model.getUid());
                            dataSend.put(Common.TO_NAME, model.getEmail());

                            request.setTo(dataSnapshot.child(model.getUid()).getValue(String.class));
                            request.setData(dataSend);

                            compositeDisposable.add(ifcmService.sendFriendRequestToUser(request)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<MyResponse>() {
                                        @Override
                                        public void accept(MyResponse myResponse) throws Exception {
                                            if(myResponse.success == 1)
                                                Toast.makeText(AllPeopleActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(AllPeopleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
    }

    @Override
    protected void onStop() {
        if(adapter != null){
            adapter.stopListening();
        }
        if(searchAdapter != null)
            searchAdapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.startListening();
            if(searchAdapter != null)
                searchAdapter.startListening();
        }
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {
        //searchBar.setLastSuggestions(lstEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}