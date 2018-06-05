package hr.helloworld.david.esports;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity implements OnItemSelectedListener {

    private TextView editId;
    private TextView editRadius;
    private TextView editDuration;
    private TextView editSize;
    private TextView editTime;
    private TextView editSport;

    int mHour;
    int mMinute;

    private String date_time;
    private DatePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        editSport.setVisibility(View.INVISIBLE);

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

        Spinner dropdown = findViewById(R.id.spinnerAdd);
        dropdown.setOnItemSelectedListener(this);
        String[] items = new String[]{"nogomet", "košarka", "rukomet", "ostalo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                editSport.setText("nogomet");
                break;
            case 1:
                editSport.setText("košarka");
                break;
            case 2:
                editSport.setText("rukomet");
                break;
            case 3:
                editSport.setVisibility(View.VISIBLE);
                break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

}
