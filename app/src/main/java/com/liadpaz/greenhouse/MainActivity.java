package com.liadpaz.greenhouse;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.liadpaz.greenhouse.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "MAIN_ACTIVITY";
    private static final int FIREBASE_AUTH = 275;

    private volatile AtomicBoolean inTask = new AtomicBoolean(false);

    private FirebaseAuth auth;
    private DocumentReference userRef;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMain);

        auth = FirebaseAuth.getInstance();
        binding.btnMainInspector.setOnClickListener((v) -> {
            if (!inTask.get()) {
                Utilities.checkConnection().thenApply(connection -> {
                    if (connection) {
                        if (auth.getCurrentUser() == null) {
                            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build())).build(), FIREBASE_AUTH);
                        } else {
                            Utilities.setRole(MainActivity.this, auth.getCurrentUser().getDisplayName());
                            checkFarms();
                        }
                    } else {
                        Utilities.setRole(MainActivity.this, Utilities.getName());
                        startActivity(new Intent(MainActivity.this, GreenhouseSelectActivity.class).putExtra(Constants.GreenhouseSelectExtra.FARM, Utilities.getId()));
                    }
                    return null;
                });
            } else {
                Utilities.checkConnection().thenApplyAsync(connection -> {
                    if (!connection) {
                        inTask.set(false);
                    }
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show());
                    return null;
                });
            }
        });
        binding.btnMainSpray.setOnClickListener((v -> {
            if (!inTask.get()) {
                Utilities.checkConnection().thenApply(connection -> {
                    Utilities.setRole(MainActivity.this);
                    if (connection) {
                        runOnUiThread(() -> {
                            Dialog farmDialog = new FarmIdSelectDialog(MainActivity.this);
                            farmDialog.setOnDismissListener(dialog -> inTask.set(false));
                            farmDialog.show();
                        });
                    } else {
                        startActivity(new Intent(MainActivity.this, GreenhouseSelectActivity.class).putExtra(Constants.GreenhouseSelectExtra.FARM, Utilities.getId()));
                    }
                    return null;
                });
            } else {
                Utilities.checkConnection().thenApplyAsync(connection -> {
                    if (!connection) {
                        inTask.set(false);
                    }
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show());
                    return null;
                });
            }
        }));

        Json.setJson(getSharedPreferences(Constants.SharedPrefConstants.BUGS, 0), getSharedPreferences(Constants.SharedPrefConstants.FARM, 0));

        Utilities.checkConnection().thenApplyAsync(connection -> {
            if (connection) {
                if (auth.getCurrentUser() != null) {
                    inTask.set(true);
                    auth.getCurrentUser().reload().addOnCompleteListener(task -> {
                        if (auth.getCurrentUser() != null) {
                            userRef = FirebaseFirestore.getInstance().collection(Constants.FirebaseConstants.USERS).document(auth.getCurrentUser().getUid());
                            Utilities.setRole(MainActivity.this, auth.getCurrentUser().getDisplayName());
                            inTask.set(false);
                            checkFarms();
                        } else {
                            inTask.set(false);
                        }
                    });
                }
            }
            return null;
        });

        // check if 12 hours or more has passed since last bug fetch / upload
        if (((new Date().getTime() - Json.JsonFarm.getLastUpdate().getTime()) / (1000.0 * 60.0 * 60.0)) >= 6) {
            Json.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (auth.getCurrentUser() == null) {
            menu.findItem(R.id.menu_main_logout).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_logout) {
            if (!inTask.get()) {
                AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        item.setVisible(false);
                        Toast.makeText(MainActivity.this, R.string.logout_successful, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function iterates over all the user's farms and if there are more than one, shows a
     * select dialog
     */
    @SuppressWarnings("ConstantConditions")
    private void checkFarms() {
        inTask.set(true);
        HashMap<String, String> farms = new HashMap<>();
        farms.put(getString(R.string.choose_farm), "");
        userRef.collection(Constants.FirebaseConstants.FARMS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot admins = task.getResult();
                if (admins != null) {
                    List<DocumentSnapshot> userAdmins = admins.getDocuments();
                    if (userAdmins != null && userAdmins.size() == 1) {
                        startActivity(new Intent(MainActivity.this, GreenhouseSelectActivity.class).putExtra(Constants.GreenhouseSelectExtra.FARM, userAdmins.get(0).getId()));
                    } else if (userAdmins != null) {
                        userAdmins.forEach(admin -> farms.put(admin.get(Constants.FirebaseConstants.NAME).toString(), admin.getId()));
                        runOnUiThread(new FarmSelectDialog(MainActivity.this, farms)::show);
                    }
                }
            }
            inTask.set(false);
        });

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FIREBASE_AUTH) {
            if (resultCode == RESULT_OK) {
                userRef = FirebaseFirestore.getInstance().collection(Constants.FirebaseConstants.USERS).document(auth.getCurrentUser().getUid());
                Toolbar toolbar = findViewById(R.id.toolbar_main);
                toolbar.getMenu().findItem(R.id.menu_main_logout).setVisible(true);
                Utilities.setRole(MainActivity.this, auth.getCurrentUser().getDisplayName());
                checkFarms();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
