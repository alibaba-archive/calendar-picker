package com.hqyxjy.ldf.supercalendar;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;

import java.util.ArrayList;

public class ExampleActivity extends AppCompatActivity implements OnSelectDateListener {

    private TextView textViewYearDisplay;
    private TextView textViewMonthDisplay;
    private View scrollSwitch;
    private MonthPager monthPager;
    private ViewGroup monthPagerContainer;
    private TextView backToday;
    private TextView hideView;

    private final int TAG_HIDE = 1;
    private final int TAG_SHOW = 2;

    private LinearLayout linearLayout;

    private CalendarDate currentDate;

    private CalendarViewAdapter calendarAdapter;

    private ArrayList<Calendar> currentCalendars = new ArrayList<>();
    private boolean initiated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        scrollSwitch = findViewById(R.id.scroll_switch);
        monthPager = (MonthPager) findViewById(R.id.calendar_view);
        monthPagerContainer = (ViewGroup) findViewById(R.id.calendar_view_container);
        textViewYearDisplay = (TextView) findViewById(R.id.show_year_view);
        textViewMonthDisplay = (TextView) findViewById(R.id.show_month_view);
        backToday = (TextView) findViewById(R.id.back_today_button);
        linearLayout = (LinearLayout) findViewById(R.id.ll_month);
        hideView = (TextView) findViewById(R.id.hide_view);
        hideView.setTag(TAG_SHOW);

        initCurrentDate();

        backToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBackToDayBtn();
            }
        });

        hideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                if (tag == TAG_SHOW) {
                    Animation animation = new AlphaAnimation(1.0f, 0f);
                    animation.setDuration(200);
                    linearLayout.setAnimation(animation);
                    linearLayout.startAnimation(animation);
                    linearLayout.setVisibility(View.GONE);
                    hideView.setTag(TAG_HIDE);
                } else if (tag == TAG_HIDE) {
                    Animation animation = new AlphaAnimation(0f, 1.0f);
                    animation.setDuration(200);
                    linearLayout.setAnimation(animation);
                    linearLayout.startAnimation(animation);
                    linearLayout.setVisibility(View.VISIBLE);
                    hideView.setTag(TAG_SHOW);
                }

            }
        });

        CustomDayView customDayView = new CustomDayView(this, R.layout.custom_day);
        calendarAdapter = new CalendarViewAdapter(
            this,
            this,
            CalendarAttr.CalendarType.MONTH,
            customDayView);
        monthPager.setAdapter(calendarAdapter);
        monthPager.setViewHeight(Utils.dpi2px(this, 270));
        monthPager.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        monthPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                position = (float) Math.sqrt(1 - Math.abs(position));
                page.setAlpha(position);
            }
        });

        monthPager.addOnPageChangeListener(new MonthPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentCalendars = calendarAdapter.getPagers();
                if (currentCalendars.get(position % currentCalendars.size()) instanceof Calendar) {
                    CalendarDate date = currentCalendars.get(position % currentCalendars.size()).getSeedDate();
                    textViewYearDisplay.setText(date.getYear() + "年");
                    textViewMonthDisplay.setText(date.getMonth() + "");

                    Log.i("onPageSelected", date + "--" + position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        scrollSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarAdapter.getCalendarType() == CalendarAttr.CalendarType.WEEK) {
                    calendarAdapter.switchToMonth();
                    ValueAnimator animator = ValueAnimator.ofFloat(1, 100);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(200);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float fraction = animation.getAnimatedFraction();

                            ViewGroup.LayoutParams containerParams = monthPagerContainer.getLayoutParams();
                            containerParams.height = monthPager.getCellHeight() + (int) (monthPager.getCellHeight() * 5 * fraction);
                            monthPagerContainer.setLayoutParams(containerParams);

                            LinearLayout.LayoutParams childParams = (LinearLayout.LayoutParams) monthPager.getLayoutParams();
                            childParams.topMargin = (int) (-monthPager.getTopMovableDistance() * (1 - fraction));
                            monthPager.setLayoutParams(childParams);
                        }
                    });
                    animator.start();
                } else {
                    calendarAdapter.switchToWeek(monthPager.getRowIndex());
                    ValueAnimator animator = ValueAnimator.ofFloat(1, 100);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(200);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float fraction = animation.getAnimatedFraction();

                            ViewGroup.LayoutParams containerParams = monthPagerContainer.getLayoutParams();
                            containerParams.height = (int) (monthPager.getCellHeight() * (6 - 5 * fraction));
                            monthPagerContainer.setLayoutParams(containerParams);

                            LinearLayout.LayoutParams childParams = (LinearLayout.LayoutParams) monthPager.getLayoutParams();
                            childParams.topMargin = (int) (-fraction * monthPager.getTopMovableDistance());
                            monthPager.setLayoutParams(childParams);
                        }
                    });
                    animator.start();
                }
            }
        });
    }

    private void onClickBackToDayBtn() {
        refreshMonthPager();
    }

    private void refreshMonthPager() {
        CalendarDate today = new CalendarDate();
        calendarAdapter.notifyDataChanged(today);
        textViewYearDisplay.setText(today.getYear() + "年");
        textViewMonthDisplay.setText(today.getMonth() + "");
    }

    /**
     * 初始化currentDate
     *
     * @return void
     */
    private void initCurrentDate() {
        currentDate = new CalendarDate();
        textViewYearDisplay.setText(currentDate.getYear() + "年");
        textViewMonthDisplay.setText(currentDate.getMonth() + "");
    }

    private void refreshClickDate(CalendarDate date) {
        currentDate = date;
        textViewYearDisplay.setText(date.getYear() + "年");
        textViewMonthDisplay.setText(date.getMonth() + "");
    }

    @Override
    public void onSelectDate(CalendarDate date) {
        refreshClickDate(date);
    }

    @Override
    public void onSelectOtherMonth(int offset) {
        //偏移量 -1表示刷新成上一个月数据 ， 1表示刷新成下一个月数据
        monthPager.selectOtherMonth(offset);
    }

    /**
     * onWindowFocusChanged回调时，将当前月的种子日期修改为今天
     *
     * @return void
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !initiated) {
            refreshMonthPager();
            initiated = true;
        }
    }
}
