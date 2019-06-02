package com.lancy.bookreview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout linearHeaderProgress;

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
        updateLoginLogoutButton();

        ActionBarDrawerToggle toggle;
        toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateUserInfo();

        if (savedInstanceState == null) {
            openSearchFragment();
        }
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
                    openFragment(new LoginActivity());
                }
                break;
            case R.id.main_wishlist_button:
                openFragment(new WishlistFragment());
                break;
            case R.id.main_sell_button:
                openFragment(new SellFragment());
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

        if (userLoggedIn()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            String emailID = user.getEmail();
            int index = emailID.indexOf('@');

            String username = emailID.substring(0, index);
            username = username.substring(0, 1).toUpperCase() + username.substring(1);

            userNameTextView.setText(username);
            emailTextView.setText(emailID);
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
        updateLoginLogoutButton();
        updateUserInfo();
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
}
