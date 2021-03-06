package com.example.aaron.fragmenttest;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Connections extends AppCompatActivity {

    private static final String FIREBASE_STORAGE_BUCKET = "gs://unisin-1351.appspot.com";
    private ArrayList<Friend> friends;
    private int numFriendsLoaded;
    private int numFriendsToLoad;
    ListView hold;
    FriendsAdapter friendsAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        // Set connections (users who aren't you)
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();


        Display d = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point p = getDisplaySize(d);


        mDatabase.child("connections/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                numFriendsLoaded = 0;
                numFriendsToLoad = (int) snapshot.getChildrenCount();
                friends = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {


                        String connectionId = child.getKey();
                        loadConnection(connectionId);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadConnection(String connectionId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users/" + connectionId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                String profilePictureURI = user.profilePictureURI;
                String displayName = user.displayName;
                String location = user.location;
                String connectionId = snapshot.getKey();

                Friend temp = new Friend(Uri.parse(profilePictureURI), displayName, location, connectionId);
                friends.add(temp);
                numFriendsLoaded++;
                if (numFriendsLoaded == numFriendsToLoad) addFriends();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addFriends(){
        hold = (ListView) findViewById(R.id.friends_list);
        hold.setBackgroundColor(Color.parseColor("#d6dbe1"));

        final Friend[] temp =  friends.toArray(new Friend[friends.size()]);
        List<Friend> friendsList = Arrays.asList(temp);

        EditText search = (EditText) findViewById(R.id.inputSearch);
        friendsAdapter = new FriendsAdapter(this,friendsList);

        hold.setAdapter(friendsAdapter);

        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                int textLength = cs.length();
                ArrayList<Friend> tempArrayList = new ArrayList<>();
                for(Friend c: temp){
                    if (textLength <= c.getName().length()) {
                        if (c.getName().toLowerCase().contains(cs.toString().toLowerCase())) {
                            tempArrayList.add(c);
                        }
                    }
                }
                friendsAdapter = new FriendsAdapter(Connections.this, tempArrayList);
                hold.setAdapter(friendsAdapter);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }




    public void alert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private Point scaleImage(ImageView view) throws NoSuchElementException {
        Display d = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point p = getDisplaySize(d);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(10, 10, 5, 5);
        params.width = p.x/3 - 15;
        params.height = p.y * 2 / 7 ;
        view.setLayoutParams(params);
        return p;
    }
    private static Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }




}
