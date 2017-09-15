package com.hubble.bta;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

/**
 * Created by songn_000 on 16 Aug 2016.
 */
public class YValueFormatter implements AxisValueFormatter {

  public YValueFormatter() {

  }

  @Override
  public String getFormattedValue(float value, AxisBase axis) {
    long lvalue = (long) value;
    return lvalue + "";
  }

  @Override
  public int getDecimalDigits() {
    return 0;
  }
}
