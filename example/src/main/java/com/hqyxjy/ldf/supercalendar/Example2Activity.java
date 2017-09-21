package com.hqyxjy.ldf.supercalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ldf.calendar.Utils;
import com.ldf.calendar.view.CalendarPickerFragment;
import com.ldf.calendar.view.PickerListener;

import java.text.DateFormat;
import java.util.Date;

public class Example2Activity extends AppCompatActivity implements PickerListener {

    private TextView textViewYearDisplay;
    private TextView textViewMonthDisplay;
    private View scrollSwitch;
    private TextView backToday;
    private TextView hideView;

    CalendarPickerFragment pickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example2);

        scrollSwitch = findViewById(R.id.scroll_switch);
        textViewYearDisplay = (TextView) findViewById(R.id.show_year_view);
        textViewMonthDisplay = (TextView) findViewById(R.id.show_month_view);
        backToday = (TextView) findViewById(R.id.back_today_button);
        hideView = (TextView) findViewById(R.id.hide_view);

        pickerFragment = new CalendarPickerFragment();
        pickerFragment.setPickerListener(this);
        getSupportFragmentManager().beginTransaction()
            .add(R.id.calendar_picker_wrapper, pickerFragment)
            .commit();

        backToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickerFragment.pickDate(new Date());
            }
        });

        scrollSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerFragment.changeMode();
            }
        });
    }

    @Override
    public void onDateFocused(Date date) {
        textViewYearDisplay.setText(date.toString());
    }
}
