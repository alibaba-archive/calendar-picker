package com.ldf.calendar.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Scroller;

import com.ldf.calendar.Utils;
import com.ldf.calendar.view.MonthPager;

public class RecyclerViewBehavior extends CoordinatorLayout.Behavior<RecyclerView> {
    private int initOffset = -1;
    private int minOffset = -1;
    private Context context;
    private boolean initiated = false;

    public RecyclerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        MonthPager monthPager = getMonthPager(parent);
        initMinOffsetAndInitOffset(parent, child, monthPager);
        return true;
    }

    private void initMinOffsetAndInitOffset(CoordinatorLayout parent,
                                            RecyclerView child,
                                            MonthPager monthPager) {
        if (monthPager.getBottom() > 0 && initOffset == -1) {
            initOffset = monthPager.getViewHeight();
            saveTop(initOffset);
            Log.e("RecyclerView--initMin1", "initOffset:" + initOffset + "bottom:" + monthPager.getBottom());
        }
        if (!initiated) {
            initOffset = monthPager.getViewHeight();
            saveTop(initOffset);
            initiated = true;
        }
        child.offsetTopAndBottom(Utils.loadTop());
        minOffset = getMonthPager(parent).getCellHeight();
        Log.e("RecyclerView--initMin2", "initOffset:" + initOffset + "--top:" + Utils.loadTop());
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        Log.e("ldf", "onStartNestedScroll");
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) child.getLayoutManager();
        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
            return false;
        }
        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        if (monthPager.getPageScrollState() != ViewPager.SCROLL_STATE_IDLE) {
            return false;
        }
        monthPager.setScrollable(false);
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        int firstRowVerticalPosition =
            (child == null || child.getChildCount() == 0) ? 0 : child.getChildAt(0).getTop();
        boolean recycleViewTopStatus = firstRowVerticalPosition >= 0;
        Log.i("RecyclerView--onStart", "PageScrollState:" + monthPager.getPageScrollState() + "---childTop:" + child.getChildAt(0).getTop()
            + "--recycleViewTopStatus:" + recycleViewTopStatus);
        return isVertical
            && (recycleViewTopStatus || !Utils.isScrollToBottom())
            && child == directTargetChild;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RecyclerView child,
                                  View target, int dx, int dy, int[] consumed) {
        Log.e("ldf", "onNestedPreScroll");
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        if (child.getTop() <= initOffset && child.getTop() >= getMonthPager(coordinatorLayout).getCellHeight()) {
            consumed[1] = Utils.scroll(child, dy,
                getMonthPager(coordinatorLayout).getCellHeight(),
                getMonthPager(coordinatorLayout).getViewHeight());
            saveTop(child.getTop());
            Log.i("RecyclerView--onNested", "loadTop:" + Utils.loadTop() + "---initOffset:" + initOffset
                + "--getTouchSlop:" + Utils.getTouchSlop(context) + "--CellHeight:"
                + getMonthPager(coordinatorLayout).getCellHeight() + "child.getTop:" + child.getTop());
        }
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout parent, final RecyclerView child, View target) {
        Log.e("ldf", "onStopNestedScroll");
        super.onStopNestedScroll(parent, child, target);
        MonthPager monthPager = (MonthPager) parent.getChildAt(0);
        monthPager.setScrollable(true);
        if (!Utils.isScrollToBottom()) {
            if (initOffset - Utils.loadTop() > Utils.getTouchSlop(context)) {
                Utils.scrollTo(parent, child, getMonthPager(parent).getCellHeight(), 200);
            } else {
                Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 80);
            }
        } else {
            if (Utils.loadTop() - minOffset > Utils.getTouchSlop(context)) {
                Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 200);
            } else {
                Utils.scrollTo(parent, child, getMonthPager(parent).getCellHeight(), 80);
            }
        }
        Log.i("RecyclerView--onStop", "loadTop:" + Utils.loadTop() + "---initOffset:" + initOffset + "--getTouchSlop:" + Utils.getTouchSlop(context));
    }

    private MonthPager getMonthPager(CoordinatorLayout coordinatorLayout) {
        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        return monthPager;
    }

    private void saveTop(int top) {
        Utils.saveTop(top);
        Log.i("RecyclerView---saveTop1", "top:" + top + "---loadTop:" + Utils.loadTop() + "---initOffset:" + initOffset);
        if (Utils.loadTop() == initOffset) {
            Utils.setScrollToBottom(false);
        } else if (Utils.loadTop() == minOffset) {
            Utils.setScrollToBottom(true);
        }
        Log.i("RecyclerView---saveTop1", "top:" + top + "---loadTop:" + Utils.loadTop() + "---initOffset:" + initOffset);
    }
}
