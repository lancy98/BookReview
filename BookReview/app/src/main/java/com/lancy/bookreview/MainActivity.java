package com.lancy.bookreview;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.LoginHandlerInterface {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout linearHeaderProgress;
    private DatabaseReference userDatabaseReference;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearHeaderProgress = findViewById(R.id.linearHeaderProgress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.draw_layout);

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
                openFragment(new AccountInformationFragment());
                drawerLayout.closeDrawer(Gravity.LEFT, true);
            }
        });

        if (savedInstanceState == null) {
            openSearchFragment();
        }

        getUserDatabaseReference();
        getUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLoginInfo();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_search_button:
                openSearchFragment();
                break;
            case R.id.main_login_logout_button:
                if (userLoggedIn()) {
                    signOut();
                } else {
                    LoginFragment fragment = new LoginFragment();
                    fragment.handler = this;
                    openFragment(fragment);
                }
                break;
            case R.id.main_wishlist_button:
                SellAndWishlistFragment wishlistFragment =
                        new SellAndWishlistFragment();
                wishlistFragment.configure(SellAndWishlistFragment.ScreenType.ScreenTypeWishlist);
                openFragment(wishlistFragment);

                break;
            case R.id.main_sell_button:
                SellAndWishlistFragment sellFragment = new SellAndWishlistFragment();
                sellFragment.configure(SellAndWishlistFragment.ScreenType.ScreenTypeSell);
                openFragment(sellFragment);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void openFragment(Fragment activity) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, activity).commit();
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
        updateLoginInfo();
    }

    private void updateLoginInfo() {
        resetUserImage();
        updateLoginLogoutButton();
        updateUserInfo();
        getUserImage();
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
        openFragment(new SearchActivity());
    }

    public void showProgressUI() {
        linearHeaderProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgressUI() {
        linearHeaderProgress.setVisibility(View.GONE);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void signInCompleted(boolean success) {
        if (success) {
            updateUserInfo();
            updateLoginLogoutButton();
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
}
