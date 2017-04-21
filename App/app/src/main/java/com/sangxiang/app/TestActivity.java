package com.sangxiang.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {
    @Bind(R.id.txt)
    TextView mDueDate;
    @Bind(R.id.due_date_picker)
    DatePicker mDatePicker;
    @Bind(R.id.menstruation_cycle_picker)
    NumberPicker mMenstruationCyclePicker;

//    @BindView(R.id.due_date_picker)
//    DatePicker mDatePicker;
//    @BindView(R.id.txt)
//    TextView mDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        setDatePickerDividerColor(mDatePicker, R.color.datapick, R.dimen.driver_height);
        mDatePicker.setCalendarViewShown(false);
        mDatePicker.setSpinnersShown(true);
        if (!BuildConfig.DEBUG) {
            mDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        }
        Calendar defaultDueDate = Calendar.getInstance();
        mDatePicker.setMinDate(resetToDay(System.currentTimeMillis()));
        mDatePicker.setMaxDate(resetToDay(getMaxDay(new Date())));
        mDatePicker.init(
                defaultDueDate.get(Calendar.YEAR),
                defaultDueDate.get(Calendar.MONTH),
                defaultDueDate.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mDueDate.setText(getString(R.string.year_month_day, year, monthOfYear + 1, dayOfMonth));
                    }
                });


        setNumberPickerDividerColor(mMenstruationCyclePicker, R.color.datapick);
        setNumberPickerDividerHeight(mMenstruationCyclePicker, R.dimen.driver_height);
        setupNumberPicker(mMenstruationCyclePicker);
        if (!BuildConfig.DEBUG) {
            mMenstruationCyclePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        }
        mMenstruationCyclePicker.setMinValue(1);
        mMenstruationCyclePicker.setMaxValue(30);
        mMenstruationCyclePicker.setValue(28);
        mMenstruationCyclePicker.setWrapSelectorWheel(false);
        mMenstruationCyclePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return getString(R.string.due_date_menstruation_cycle_select, value);
            }
        });
        mMenstruationCyclePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e("sangxiang", "onValueChange: "+ newVal);
            }
        });

    }

    /**
     * Set date picker divider color
     * <p>
     * Reflection based method
     *
     * @param datePicker
     * @param color
     * @param height
     */
    @Keep
    public static void setDatePickerDividerColor(final @NonNull DatePicker datePicker,
                                                 final @ColorRes int color,
                                                 final @DimenRes int height) {
        // Divider changing:
        Context context = datePicker.getContext();
        // Get mSpinners
        LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);
        // Get NumberPicker
        LinearLayout mSpinners = (LinearLayout) llFirst.getChildAt(0);
        for (int i = 0; i < mSpinners.getChildCount(); i++) {
            View view = mSpinners.getChildAt(i);
            if (view != null && view instanceof NumberPicker) {
                NumberPicker picker = (NumberPicker) view;
                setNumberPickerDividerColor(picker, color);
                setNumberPickerDividerHeight(picker, height);
            }
        }
    }

    /**
     * @param picker
     * @param height
     */
    @Keep
    public static void setNumberPickerDividerHeight(final @NonNull NumberPicker picker,
                                                    final @DimenRes int height) {

        Context context = picker.getContext();

        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDividerHeight")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, context.getResources().getDimensionPixelSize(height));
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * @param picker
     * @param color
     */
    @Keep
    public static void setNumberPickerDividerColor(final @NonNull NumberPicker picker,
                                                   final @ColorRes int color) {
        Context context = picker.getContext();

        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(ContextCompat.getColor(context, color)));
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    //取当天日期的20170421 00 ：00：00
    public static long resetToDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getMaxDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 279);
        System.out.println(calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH));
        return calendar.getTimeInMillis();
    }



    /**
     * When we use formatter in number picker, it won't use it render the default value when display.
     * @param picker
     */
    public static void setupNumberPicker(final @NonNull NumberPicker picker) {
        Field f = null;
        try {
            f = NumberPicker.class.getDeclaredField("mInputText");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (f == null) return;
        try {
            EditText inputText = (EditText) f.get(picker);
            inputText.setFilters(new InputFilter[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
