package com.example.aaron.fragmenttest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ActivityFeed extends Fragment {
    private ListView container;
    private SwipeRefreshLayout swipe;
    private List<SharedNotification> notificationsList;
    private FirebaseWaitLoader loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activity_feed
                , container, false);
        return v;
    }
    public static ActivityFeed newInstance(String text) {

        ActivityFeed f = new ActivityFeed();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    public void onStart(){
        Display d = ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point p = getDisplaySize(d);

        container = (ListView) getActivity().findViewById(R.id.activity_list);
        container.setBackgroundColor(Color.parseColor("#d6dbe1"));


        container.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(mLastFirstVisibleItem<firstVisibleItem)
                {
                    Log.i("SCROLLING DOWN","TRUE");
                }
                if(mLastFirstVisibleItem>firstVisibleItem)
                {
                    Log.i("SCROLLING UP","TRUE");
                }
                mLastFirstVisibleItem=firstVisibleItem;

            }
        });

        swipe = (SwipeRefreshLayout) getActivity().findViewById(R.id.activity_feed_swipe_refresh);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Toast.makeText(getActivity(), "refreshing", Toast.LENGTH_LONG).show();
                        loadNotifications();
                    }
                }
        );


        /*Need an object to store activity feed data, will do the same thing with
        the calendar stuff too, this may also be a good way to do friends, as it will endable for easy searching

        Create an object that holds the uri for the friends profile pic and whatever data we are trying to portay and then
        set it via the   adapter
        */




        loadNotifications();



        super.onStart();
    }


    public void loadNotifications() {
        notificationsList = new ArrayList<>();
        MainActivity activity = (MainActivity) getActivity();
        FirebaseUser user = activity.getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference notifications = database.child("notifications").child(user.getUid());
        notifications.limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                loader = new FirebaseWaitLoader((int) snapshot.getChildrenCount());
                for (DataSnapshot notification : snapshot.getChildren()) {
                    String notificationId = notification.getKey();
                    loadNotification(notificationId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadNotification(String notificationId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference notification = database.child("shared-notifications").child(notificationId);
        notification.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SharedNotification notification = snapshot.getValue(SharedNotification.class);
                notificationsList.add(0, notification);

                loader.update();

                if (loader.done()) {
                    // End refreshing
                    swipe.setRefreshing(false);

                    // Populate notifications
                    container.setAdapter(new ActivityFeedAdapter(getActivity(), notificationsList));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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