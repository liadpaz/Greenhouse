package com.liadpaz.greenhouse;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class GreenhouseActivity extends AppCompatActivity {

    private String farm;
    private Greenhouse greenhouse;

    private ArrayList<Bug> bugs = new ArrayList<>();

    private ConstraintLayout layout_inner_greenhouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouse);

        farm = Utilities.getFarm();
        if (Utilities.getRole() == Utilities.Role.Inspector) {
            Utilities.setName(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
        }
        greenhouse = (Greenhouse) getIntent().getSerializableExtra("Greenhouse");

        layout_inner_greenhouse = findViewById(R.id.layout_inner_greenhouse);

        layout_inner_greenhouse.setY(0);
        layout_inner_greenhouse.setX(0);

        int width = getResources().getDisplayMetrics().widthPixels;

        //noinspection SuspiciousNameCombination
        layout_inner_greenhouse.setLayoutParams(new ConstraintLayout.LayoutParams(width, width));

        layout_inner_greenhouse.setBackgroundColor(Color.GREEN);


        Utilities.getBugsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bugs.clear();
                dataSnapshot.getChildren().forEach(dst -> {
                    Bug bug;
                    if ((bug = dst.getValue(Bug.class)).Greenhouse.equals(greenhouse.Id)) {
                        bugs.add(bug);
                    }
                });
                bugs.forEach(bug -> runOnUiThread(() -> addBug(bug)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addBug(Bug bug) {
        double width = getResources().getDisplayMetrics().widthPixels;

        double x = (width / Double.parseDouble(greenhouse.Width) * bug.X) - 5;
        double y = width - (width / Double.parseDouble(greenhouse.Height) * bug.Y) - 5;

        CircleView circleView = new CircleView(x, y, 5);

        layout_inner_greenhouse.addView(circleView);
    }

    private class CircleView extends View {
        private double size;

        private Paint paint;

        public CircleView(double x, double y, double size) {
            super(GreenhouseActivity.this);
            setLayoutParams(new ConstraintLayout.LayoutParams((int) (size * 2), (int) (size * 2)));

            setX((float) x);
            setY((float) y);
            this.size = size;
            this.paint = new Paint();
            paint.setColor(Color.BLACK);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle((float) size, (float) size, (float) size, paint);
        }
    }
}
