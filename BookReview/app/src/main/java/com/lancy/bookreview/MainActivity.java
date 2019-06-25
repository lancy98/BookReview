package com.lancy.bookreview;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lancy.bookreview.bookscreens.SearchFragment;
import com.lancy.bookreview.helper.UserInterfaceHelper;
import com.lancy.bookreview.model.User;
import com.lancy.bookreview.userflow.AccountInformationFragment;
import com.lancy.bookreview.userflow.LoginFragment;
import com.lancy.bookreview.wishlistandsell.SellAndWishlistFragment;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.LoginHandlerInterface,
        SearchFragment.SearchFragmentHandlerInterface {

    private static final int Image_Capture_Code = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CatLoadingView catLoadingView = new CatLoadingView();
    private DatabaseReference userDatabaseReference;
    private User user;

    private BroadcastReceiver firebaseNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");

            // Show the notification alert.
            new SweetAlertDialog(MainActivity.this)
                    .setTitleText(title)
                    .setContentText(body)
                    .setConfirmText("Dismiss")
                    .show();
        }
    };

    private BroadcastReceiver userDataChangedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLoginInfo();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        catLoadingView.setCanceledOnTouchOutside(false);
        drawerLayout = findViewById(R.id.draw_layout);
        addDrawerListener();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle;
        toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInterfaceHelper.hasUserLoggedIn()) {
                    openFragment(new AccountInformationFragment());
                    drawerLayout.closeDrawer(Gravity.LEFT, true);
                }
            }
        });

        if (savedInstanceState == null) {
            openSearchFragment();
        }

        updateLoginInfo();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(userDataChangedMessageReceiver,
                        new IntentFilter("User Data Changed"));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(userDataChangedMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLoginInfo();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(firebaseNotificationReceiver,
                new IntentFilter("Firebase Notification"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(firebaseNotificationReceiver);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_search_button:
                openSearchFragment();
                break;
            case R.id.main_wishlist_button:
                handleWishlistButtonTap();
                break;
            case R.id.main_sell_button:
                handleSellButtonTap();
                break;
            case R.id.main_login_logout_button:
                handleLoginLogoutButtonTap();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleSellButtonTap() {
        if (UserInterfaceHelper.hasUserLoggedIn()) {
            SellAndWishlistFragment sellFragment = new SellAndWishlistFragment();
            sellFragment.configure(SellAndWishlistFragment.ScreenType.ScreenTypeSell);
            openFragment(sellFragment);
        } else {
            // Show the notification alert.
            new SweetAlertDialog(MainActivity.this)
                    .setTitleText("Sign In")
                    .setContentText("Please sign in to use this feature")
                    .setConfirmText("Dismiss")
                    .show();
        }
    }

    private void handleWishlistButtonTap() {
        if (UserInterfaceHelper.hasUserLoggedIn()) {
            SellAndWishlistFragment wishlistFragment =
                    new SellAndWishlistFragment();
            wishlistFragment.configure(SellAndWishlistFragment.ScreenType.ScreenTypeWishlist);
            openFragment(wishlistFragment);
        } else {
            // Show the notification alert.
            new SweetAlertDialog(MainActivity.this)
                    .setTitleText("Sign In")
                    .setContentText("Please sign in to use this feature")
                    .setConfirmText("Dismiss")
                    .show();
        }
    }

    private void handleLoginLogoutButtonTap() {
        if (userLoggedIn()) {
            signOut();
        } else {
            LoginFragment fragment = new LoginFragment();
            fragment.handler = this;
            openFragment(fragment);
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    public void updateLoginLogoutButton() {
        Menu menu = navigationView.getMenu();
        MenuItem loginLogoutMenuItem = menu.findItem(R.id.main_login_logout_button);

        loginLogoutMenuItem.setTitle(userLoggedIn() ? "Logout" : "Login");
    }

    public void updateUserInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.usernameTextView);
        TextView emailTextView = headerView.findViewById(R.id.emailIDTextView);

        if (userLoggedIn() && user != null) {
            userNameTextView.setText(user.username);
            emailTextView.setText(user.email);
        } else {
            userNameTextView.setText("Sign In");
            emailTextView.setText("");
        }
    }

    private boolean userLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        userDatabaseReference = null;
        updateLoginInfo();

       Fragment fragment = getSupportFragmentManager()
               .findFragmentById(R.id.fragment_container);

       if (!(fragment instanceof SearchFragment)) {
           openSearchFragment();
       }
    }

    private void updateLoginInfo() {
        getUserDatabaseReference();
        resetUserImage();
        updateLoginLogoutButton();
        updateUserInfo();
        getUserImage();
        getUserData();
    }

    private void resetUserImage() {
        View headerView = navigationView.getHeaderView(0);
        CircleImageView imageView = headerView.findViewById(R.id.profileImageView);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_person, null);
        imageView.setImageDrawable(drawable);
    }


    private FirebaseUser firebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void getUserDatabaseReference() {
        FirebaseUser firebaseUser = firebaseUser();
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            userDatabaseReference = FirebaseDatabase.getInstance().
                    getReference().child("user").child(firebaseUser().getUid());
        }
    }

    private void getUserImage() {
        if (userDatabaseReference == null) {
            return;
        }

        userDatabaseReference.child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imagePath = dataSnapshot.getValue(String.class);
                if (imagePath != null) {
                    View headerView = navigationView.getHeaderView(0);
                    CircleImageView imageView = headerView.findViewById(R.id.profileImageView);
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_person, null);
                    Picasso.get().load(imagePath)
                            .placeholder(drawable)
                            .fit()
                            .centerCrop()
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData() {
        if (userDatabaseReference == null) {
            return;
        }

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                User.setCurrentUser(user);
                updateUserInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void openSearchFragment() {
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.handler = this;
        openFragment(searchFragment);
    }

    public void showCamera() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, Image_Capture_Code);
                    }

                    @Override public void onPermissionDenied(PermissionDeniedResponse response) { }

                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                             PermissionToken token) { }

                }).check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                SearchFragment searchFragment =  (SearchFragment )
                        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                searchFragment.convertImageToTextAndLoadBooks(bitmap);
            }
        }
    }

    public void showProgressUI() {
        catLoadingView.show(getSupportFragmentManager(), "");
    }

    public void hideProgressUI() {
        catLoadingView.dismiss();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void signInCompleted(boolean success) {
        if (success) {
            getUserDatabaseReference();
            updateLoginInfo();
            openSearchFragment();
        }
    }

    @Override
    public void hideProgressView() {
        hideProgressUI();
    }

    @Override
    public void showProgressView() {
        showProgressUI();
    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    private void addDrawerListener() {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                hideKeyboard();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }
}
