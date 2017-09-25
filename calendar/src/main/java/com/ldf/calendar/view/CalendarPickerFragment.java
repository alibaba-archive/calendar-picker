package com.ldf.calendar.view;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.mi.calendar.R;

import java.util.Date;

/**
 * Created by dali on 2017/9/21.
 */
public class CalendarPickerFragment extends Fragment implements OnSelectDateListener, PickerListener {

    private PickerListener pickerListener;

    private ViewGroup monthPagerContainer;
    private MonthPager monthPager;

    private CalendarViewAdapter calendarAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_picker, container, false);
        monthPagerContainer = (ViewGroup) view.findViewById(R.id.calendar_picker_container);
        monthPager = (MonthPager) view.findViewById(R.id.calendar_picker);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCalendarPicker();
    }

    private void initCalendarPicker() {
        CustomDayView customDayView = new CustomDayView(getContext(), R.layout.custom_day);
        calendarAdapter = new CalendarViewAdapter(
            getContext(),
            this,
            CalendarAttr.CalendarType.MONTH,
            customDayView);
        calendarAdapter.setPickerListener(this);
        monthPager.setAdapter(calendarAdapter);
        monthPager.setViewHeight(Utils.dpi2px(getContext(), 270));
        monthPager.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        monthPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                position = (float) Math.sqrt(1 - Math.abs(position));
                page.setAlpha(position);
            }
        });
    }

    public void changeViewType(CalendarAttr.CalendarType calendarType) {
        if (calendarAdapter.getCalendarType() == calendarType) {
            return;
        }
        if (calendarType == CalendarAttr.CalendarType.WEEK) {
            calendarAdapter.switchToWeek(monthPager.getRowIndex());
            ViewGroup.LayoutParams containerParams = monthPagerContainer.getLayoutParams();
            containerParams.height = monthPager.getCellHeight();
            monthPagerContainer.setLayoutParams(containerParams);

            FrameLayout.LayoutParams childParams = (FrameLayout.LayoutParams) monthPager.getLayoutParams();
            childParams.topMargin = -monthPager.getTopMovableDistance();
            monthPager.setLayoutParams(childParams);
        } else if (calendarType == CalendarAttr.CalendarType.MONTH) {
            calendarAdapter.switchToMonth();
            ViewGroup.LayoutParams containerParams = monthPagerContainer.getLayoutParams();
            containerParams.height = monthPager.getCellHeight() * 6;
            monthPagerContainer.setLayoutParams(containerParams);

            FrameLayout.LayoutParams childParams = (FrameLayout.LayoutParams) monthPager.getLayoutParams();
            childParams.topMargin = 0;
            monthPager.setLayoutParams(childParams);
        }
    }

    public void changeMode() {
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

                    FrameLayout.LayoutParams childParams = (FrameLayout.LayoutParams) monthPager.getLayoutParams();
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

                    FrameLayout.LayoutParams childParams = (FrameLayout.LayoutParams) monthPager.getLayoutParams();
                    childParams.topMargin = (int) (-fraction * monthPager.getTopMovableDistance());
                    monthPager.setLayoutParams(childParams);
                }
            });
            animator.start();
        }
    }

    public void pickDate(Date date) {
        calendarAdapter.notifyDataChanged(Utils.fromDate(date));
    }

    public void setPickerListener(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
    }

    @Override
    public void onDateFocused(Date date) {
        if (pickerListener != null) {
            pickerListener.onDateFocused(date);
        }
    }

    @Override
    public void onSelectDate(CalendarDate date) {
        if (pickerListener != null) {
            pickerListener.onDateFocused(Utils.toDate(date));
        }
    }

    @Override
    public void onSelectOtherMonth(int offset) {
        monthPager.selectOtherMonth(offset);
    }
}
