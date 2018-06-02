package hr.helloworld.david.esports;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private TextView editId;
    private TextView editRadius;
    private TextView editDuration;
    private TextView editSize;
    private TextView editTime;
    private TextView editSport;

    int mHour;
    int mMinute;

    private String date_time;
    private String[] options = {"nogomet", "košarka", "rukomet", "ostalo"};
    private AlertDialog.Builder builder;
    private DatePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        builder = new AlertDialog.Builder(AddEventActivity.this);
        builder.setTitle("Odaberi sport");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editSport.setText(options[which]);
            }
        });
        builder.setCancelable(true);

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                date_time = year + "-" + month + "-" + day;
                timePicker();
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        dialog = new DatePickerDialog(
                AddEventActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year, month, day);

        dialog.getDatePicker().setMinDate(Calendar.getInstance().getTime().getTime() - 1000);

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        editId = findViewById(R.id.editId);
        editRadius = findViewById(R.id.editRadius);
        editDuration = findViewById(R.id.editDuration);
        editSize = findViewById(R.id.editSize);
        editTime = findViewById(R.id.editTime);
        editSport = findViewById(R.id.editSport);

        editSport.setInputType(InputType.TYPE_NULL);
        editSport.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    builder.show();
                }
            }
        });

        editSport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        editSport.setKeyListener(null);



        //DATE and TIME pickers
        editTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.show();
                }
            }
        });

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        editTime.setKeyListener(null);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent adding = new Intent();
                adding.putExtra("id", editId.getText().toString());

                if (editId.getText().toString().matches("")) {
                    editId.setError("Required.");
                    return;
                }
                adding.putExtra("rad", editRadius.getText().toString());
                if (editRadius.getText().toString().matches("")) {
                    editRadius.setError("Required.");
                    return;
                }
                adding.putExtra("dur", editDuration.getText().toString());
                if (editDuration.getText().toString().matches("")) {
                    editDuration.setError("Required.");
                    return;
                }
                adding.putExtra("size", editSize.getText().toString());
                if (editSize.getText().toString().matches("")) {
                    editSize.setError("Required.");
                    return;
                }
                adding.putExtra("time", editTime.getText().toString());
                if (editTime.getText().toString().matches("")) {
                    editTime.setError("Required.");
                    return;
                }
                adding.putExtra("sport", editSport.getText().toString());
                if (editSport.getText().toString().matches("")) {
                    editSport.setError("Required.");
                    return;
                }
                setResult(RESULT_OK, adding);
                Snackbar.make(view, "Adding Event...", Snackbar.LENGTH_LONG).show();

                finish();
            }
        });

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SimpleDateFormat")
    private void timePicker() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        DateFormat format = new SimpleDateFormat("yyyy-M-d");


                        if (!date_time.equals(format.format(calendar.getTime()))) {
                            String date = date_time + " " + hourOfDay + ":" + minute;
                            editTime.setText(date);
                            editTime.setError(null);
                        } else {
                            boolean state = (mHour == hourOfDay && mMinute > minute);

                            if (mHour > hourOfDay || state) {
                                editTime.setError(getResources().getString(R.string.AddEventTimeError));
                            } else {
                                editTime.setError(null);
                                String date = date_time + " " + hourOfDay + ":" + minute;
                                editTime.setText(date);
                            }
                        }
                    }
                }, mHour, mMinute, true);

        timePickerDialog.show();
    }

}
