package hr.helloworld.david.esports;


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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity implements OnItemSelectedListener{

    private TextView editId;
    private TextView editRadius;
    private TextView editDuration;
    private TextView editSize;
    private TextView editTime;
    private TextView editSport;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    int mHour;
    int mMinute;

    String date_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent addIntent = getIntent();

//        TextView editLat = findViewById(R.id.edit_lat);
//        TextView editLong = findViewById(R.id.edit_long);
        editId = findViewById(R.id.editId);
        editRadius = findViewById(R.id.editRadius);
        editDuration = findViewById(R.id.editDuration);
        editSize = findViewById(R.id.editSize);
        editTime = findViewById(R.id.editTime);
        editSport = findViewById(R.id.editSport);
        editSport.setVisibility(View.INVISIBLE);

        //DATE and TIME pickers
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        AddEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("****", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                date_time = year + "-" + month + "-" + day;
                timePicker();
            }
        };

//        editLong.setText("Long: " + String.valueOf(addIntent.getDoubleExtra("Long", 0)));
//        editLat.setText("Lat: " + String.valueOf(addIntent.getDoubleExtra("Lat", 0)));

//drop down
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
                //slanje foreme u main activity
                Intent adding = new Intent();
                adding.putExtra("id", editId.getText().toString());

                if (editId.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter name", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                adding.putExtra("rad", editRadius.getText().toString());
                if (editRadius.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter radius", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                adding.putExtra("dur", editDuration.getText().toString());
                if (editDuration.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter duration", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                adding.putExtra("size", editSize.getText().toString());
                if (editSize.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a size", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                adding.putExtra("time", editTime.getText().toString());
                if (editTime.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a time", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                adding.putExtra("sport", editSport.getText().toString());
                if (editSport.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "You did not enter a sport", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                setResult(RESULT_OK, adding);
                finish();

                Snackbar.make(view, "Adding Event...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void timePicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        editTime.setText(date_time+" "+hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    //dropdown

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("**** ", "filter ON");

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



