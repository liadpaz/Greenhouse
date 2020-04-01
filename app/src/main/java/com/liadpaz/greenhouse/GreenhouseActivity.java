package com.liadpaz.greenhouse;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.liadpaz.greenhouse.databinding.ActivityGreenhouseBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GreenhouseActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "ACTIVITY_GREENHOUSE";
    private Greenhouse greenhouse;

    private ArrayList<Bug> bugs = new ArrayList<>();

    private ConstraintLayout layout_inner_greenhouse;
    private int viewWidth;
    private int viewHeight;

    private ArrayList<Bug> addedBugs;

    private TextView tv_added_bugs;

    private AtomicBoolean inTask = new AtomicBoolean(false);

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGreenhouseBinding binding = ActivityGreenhouseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarGreenhouse);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_added_bugs = binding.tvAddedBugs;
        greenhouse = (Greenhouse)getIntent().getSerializableExtra(Constants.GreenhouseExtra.GREENHOUSE);

        getSupportActionBar().setTitle(String.format("%s: %s", getString(R.string.app_name), greenhouse.getId()));

        layout_inner_greenhouse = binding.layoutInnerGreenhouse;
        binding.btnRemoveLast.setOnClickListener(v -> {
            if (!inTask.get()) {
                if (addedBugs.size() > 0) {
                    Bug bugToRemove = addedBugs.remove(addedBugs.size() - 1);
                    bugs.remove(bugToRemove);
                    removeBugView(bugToRemove);
                } else {
                    Toast.makeText(GreenhouseActivity.this, R.string.cant_remove_bug, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(GreenhouseActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
        });
        binding.btnAddBug.setOnClickListener(v -> {
            // TODO: add x and y coordinates from hardware
            Random random = new Random();
            Bug newBug = new Bug(greenhouse.getId(), new Date(), random.nextDouble() * greenhouse.getWidth(), random.nextDouble() * greenhouse.getHeight());
            addedBugs.add(newBug);
            bugs.add(newBug);
            addRedBug(newBug);
            tv_added_bugs.setText(String.format("%s: %s", getString(R.string.added_bugs), addedBugs.size()));
        });

        // get the size of the action bar
        TypedArray typedArray = obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int)typedArray.getDimension(0, -1);
        typedArray.recycle();

        // get the screen width
        int width = getResources().getDisplayMetrics().widthPixels;
        // get the ratio of the greenhouse size
        double ratio = (double)greenhouse.getHeight() / (double)greenhouse.getWidth();

        // set the greenhouse size (on screen), constraint layout
        if (ratio > 1) {
            viewWidth = (int)(width * (1 / ratio));
            //noinspection SuspiciousNameCombination
            viewHeight = width;
            layout_inner_greenhouse.setLayoutParams(new ConstraintLayout.LayoutParams(viewWidth, viewHeight));
            layout_inner_greenhouse.setX((float)((double)width / 2 * (1 - (1 / ratio))));
            layout_inner_greenhouse.setY(toolbarHeight);
        } else {
            viewWidth = width;
            viewHeight = (int)((double)width * ratio);
            layout_inner_greenhouse.setLayoutParams(new ConstraintLayout.LayoutParams(viewWidth, viewHeight));
            layout_inner_greenhouse.setX(0);
            layout_inner_greenhouse.setY((float)((double)width / 2 * (1 - ratio)) + toolbarHeight);
        }

        // set the greenhouse color to green
        layout_inner_greenhouse.setBackgroundColor(Color.GREEN);

        // add all the previously downloaded bugs to the greenhouse (on screen)
        bugs = Json.JsonBugs.getBugs(greenhouse.toString());
        bugs.forEach(this::addBlackBug);

        addedBugs = new ArrayList<>();

        if (Utilities.getRole() == Utilities.Role.Inspector) {
            // set the start bugs count to the proper count
            binding.tvStartBugs.setText(String.format("%s: %d", getString(R.string.start_bugs), bugs.size()));
            // set the added bugs count to 0
            tv_added_bugs.setText(String.format("%s: %s", getString(R.string.added_bugs), addedBugs.size()));
        } else {
            binding.tvStartBugs.setVisibility(View.INVISIBLE);
            binding.tvAddedBugs.setVisibility(View.INVISIBLE);
            binding.btnAddBug.setVisibility(View.INVISIBLE);
            binding.btnRemoveLast.setVisibility(View.INVISIBLE);
            binding.btnUploadToPi.setVisibility(View.VISIBLE);
            binding.btnUploadToPi.setOnClickListener((v -> uploadToPi()));
        }
    }

    /**
     * This function adds a red bug to the greenhouse (on screen). This is a newly found bug
     *
     * @param bug the bug to add
     */
    private void addRedBug(@NotNull Bug bug) {
        double x = ((double)viewWidth / greenhouse.getWidth() * bug.getX()) - 5;
        double y = viewHeight - ((double)viewHeight / greenhouse.getHeight() * bug.getY()) - 5;

        CircleView view = new CircleView(bug, x, y, 5, Color.RED);

        layout_inner_greenhouse.addView(view);
    }

    /**
     * This function adds a Black bug to the greenhouse (on screen). This is a bug that has already
     * found
     *
     * @param bug the bug to add
     */
    private void addBlackBug(@NotNull Bug bug) {
        double x = ((double)viewWidth / greenhouse.getWidth() * bug.getX()) - 5;
        double y = viewHeight - ((double)viewHeight / greenhouse.getHeight() * bug.getY()) - 5;

        CircleView view = new CircleView(bug, x, y, 5, Color.BLACK);

        layout_inner_greenhouse.addView(view);
    }

    /**
     * This function removes a bug from the greenhouse (on screen). Only bugs that had been found on
     * the current session
     *
     * @param bug the bug to remove
     */
    private void removeBugView(@NotNull Bug bug) {
        layout_inner_greenhouse.removeView(layout_inner_greenhouse.getViewById(bug.getId()));
        tv_added_bugs.setText(String.format("%s: %s", getString(R.string.added_bugs), addedBugs.size()));
    }

    /**
     * This function saves the bugs that have been added on the current session to the local file
     */
    private void saveBugs() {
        Json.JsonBugs.setBugs(greenhouse, bugs);
        Toast.makeText(GreenhouseActivity.this, R.string.saved_bugs, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
    }

    /**
     * This function uploads the bugs from the local file to the Raspberry Pi controller
     */
    private void uploadToPi() {
        // TODO: add method
        Toast.makeText(GreenhouseActivity.this, "Uploading", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_greenhouse, menu);
        // if the user is an exterminator; he shouldn't have the option to change the bugs hence the option to save/delete local file
        if (Utilities.getRole() == Utilities.Role.Exterminator) {
            menu.findItem(R.id.menu_save_local).setVisible(false);
            menu.findItem(R.id.menu_delete_all_added).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_local: {
                if (!inTask.get()) {
                    saveBugs();
                } else {
                    Toast.makeText(GreenhouseActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
                }
                break;
            }

            case R.id.menu_delete_all_added: {
                while (addedBugs.size() > 0) {
                    removeBugView(addedBugs.remove(addedBugs.size() - 1));
                }
                Toast.makeText(GreenhouseActivity.this, R.string.deleted_all_added, Toast.LENGTH_LONG).show();
                break;
            }

            default: {
                return false;
            }
        }
        return true;
    }

    /**
     * This class is for the bugs on screen. Circle spot on screen with id according the bug they
     * represent
     */
    private class CircleView extends View {
        private double size;

        private Paint paint = new Paint();

        public CircleView(@NotNull Bug bug, double x, double y, double size, int color) {
            super(GreenhouseActivity.this);

            setId(bug.getId());
            setLayoutParams(new ConstraintLayout.LayoutParams((int)(size * 2), (int)(size * 2)));
            setX((float)x);
            setY((float)y);
            this.size = size;
            paint.setColor(color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle((float)size, (float)size, (float)size, paint);
        }
    }
}
