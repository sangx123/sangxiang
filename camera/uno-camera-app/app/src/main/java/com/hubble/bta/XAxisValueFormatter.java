package com.hubble.bta;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.util.List;

/**
 * Created by songn_000 on 16 Aug 2016.
 */
public class XAxisValueFormatter implements AxisValueFormatter {
  private List<BarEntry> barEntries;
  public XAxisValueFormatter(List<BarEntry> barEntries) {
    this.barEntries = barEntries;
  }

  @Override
  public String getFormattedValue(float value, AxisBase axis) {
    int i = (int) value;
    if(i < barEntries.size() && i >= 0) {
      return this.barEntries.get((int) value).getData().toString();
    } else {
      return "";
    }
  }

  @Override
  public int getDecimalDigits() {
    return 0;
  }
}
