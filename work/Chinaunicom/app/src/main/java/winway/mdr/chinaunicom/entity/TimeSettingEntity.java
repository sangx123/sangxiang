package winway.mdr.chinaunicom.entity;

import java.util.Calendar;

import winway.mdr.chinaunicom.comm.DataResours;

import com.liz.cptgen.TCallPolicyState;
import com.liz.cptgen.TScenesState;
import com.liz.cptr.TTimeSceneClasses;
import com.liz.cptr.TTimeSceneData;

/***************************************
 * 时间设置界面信息
 * @author zhaohao
 * 时间:2011-11
 */
public class TimeSettingEntity {
   private int id;
   private String begintime;
   private String endtime;
   private String timesetting_status;
   private String timesetting_scene;
   private String detailrepeat;
   private int timesetting_isopen;
   private int timesetting_icon_id;
   private int durations; // 持续时长
   private int leftseconds; // 剩余秒数。
   private String bieming;
	public String getBieming() {
	return bieming;
}

public void setBieming(String bieming) {
	this.bieming = bieming;
}

	public int getDurations() {
	return durations;
}

public void setDurations(int durations) {
	this.durations = durations;
}

public int getLeftseconds() {
	return leftseconds;
}

public void setLeftseconds(int leftseconds) {
	this.leftseconds = leftseconds;
}

	public static String hourOrMinutesToStr(int hourOrMinute) {
		String ret = Integer.toString(hourOrMinute);
		if (ret.length() == 1) {
			ret = "0" + ret;
		}

		return ret;
	}

	// time scene data.
	public void setByTimeSceneData(TTimeSceneData data) {
		id = data.getIndex();

		// begin time
		Calendar calendar = data.getStartTime();
		if (calendar != null) {
			begintime = hourOrMinutesToStr(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
					+ hourOrMinutesToStr(calendar.get(Calendar.MINUTE));
		}

		// end time.
		calendar = data.getEndTime();
		if (calendar != null) {
			endtime = hourOrMinutesToStr(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
					+ hourOrMinutesToStr(calendar.get(Calendar.MINUTE));
		}

		TTimeSceneClasses.Enum classes = data.getClasses();
		if (classes != null) {
			switch (classes.intValue()) {
			case TTimeSceneClasses.INT_ONCE:
				detailrepeat = "单次";
				break;
			case TTimeSceneClasses.INT_EVERYDAY:
				detailrepeat = "每天";
				break;
			case TTimeSceneClasses.INT_WEEKLY:
				int weekStart = data.getWeekStart() - 2;
				int weekEnd = data.getWeekEnd() - 2;
				if (weekStart < 0 || weekStart > 6) {
					weekStart = 6;
				}
				if (weekEnd < 0 || weekEnd > 6) {
					weekEnd = 6;
				}
				 if(weekStart!=weekEnd)
				  detailrepeat = DataResours.datas[weekStart] + "-" + DataResours.datas[weekEnd];
				 else detailrepeat="每"+DataResours.datas[weekStart];
				break;
			}
		}

		TCallPolicyState.Enum policy = data.getPolicy();
		if (policy != null) {
			TScenesState.Enum scene = data.getScene();
			switch (policy.intValue()) {
			case TCallPolicyState.INT_ZC:
				break;
			case TCallPolicyState.INT_SM:
				timesetting_status = DataResours.status[0];
				if (scene != null) {
					switch (scene.intValue()) {
					case TScenesState.INT_XX:
						timesetting_scene = DataResours.fjwrValues[0];
						timesetting_icon_id = 0;
						break;
					case TScenesState.INT_KC:
						timesetting_scene = DataResours.fjwrValues[1];
						timesetting_icon_id = 1;
						break;
					case TScenesState.INT_KH:
						timesetting_scene = DataResours.fjwrValues[2];
						timesetting_icon_id = 2;
						break;
					case TScenesState.INT_SK:
						timesetting_scene = DataResours.fjwrValues[3];
						timesetting_icon_id = 3;
						break;
					case TScenesState.INT_CG:
						timesetting_scene = DataResours.fjwrValues[4];
						timesetting_icon_id = 4;
						break;
					case TScenesState.INT_TY:
						timesetting_scene = DataResours.fjwrValues[5];
						timesetting_icon_id = 5;
						break;
					default:
						break;
					}
				}
				break;
			case TCallPolicyState.INT_GJ:
				timesetting_status = DataResours.status[1];
				if (scene != null) {
					switch (scene.intValue()) {
					case TScenesState.INT_XX:
						timesetting_scene = DataResours.qhdrValues[0];
						timesetting_icon_id = 0;
						break;
					case TScenesState.INT_KC:
						timesetting_scene = DataResours.qhdrValues[1];
						timesetting_icon_id = 1;
						break;
					case TScenesState.INT_KH:
						timesetting_scene = DataResours.qhdrValues[2];
						timesetting_icon_id = 2;
						break;
					case TScenesState.INT_SK:
						timesetting_scene = DataResours.qhdrValues[3];
						timesetting_icon_id = 3;
						break;
					case TScenesState.INT_FJ:
						timesetting_scene = DataResours.qhdrValues[4];
						timesetting_icon_id = 4;
						break;
					case TScenesState.INT_GJ_TSY:
						timesetting_scene = DataResours.qhdrValues[5];
						timesetting_icon_id = 5;
						break;
					case TScenesState.INT_OOS:
						timesetting_scene = DataResours.qhdrValues[6];
						timesetting_icon_id = 6;
						break;
					case TScenesState.INT_CG:
						timesetting_scene = DataResours.qhdrValues[7];
						timesetting_icon_id = 7;
						break;
					case TScenesState.INT_TY:
						timesetting_scene = DataResours.qhdrValues[8];
						timesetting_icon_id = 8;
						break;
					default:
						break;
					}
				}
				break;
			}
		}
	}

	static Calendar getTimeByText(String text) {
		Calendar calendar = Calendar.getInstance();
		String tmp[] = text.split(":");
		if (tmp.length != 2) {
			return null;
		}

		int hour = Integer.parseInt(tmp[0]);
		if (hour < 0 || hour > 23) {
			return null;
		}

		int minute = Integer.parseInt(tmp[1]);
		if (minute < 0 || minute > 59) {
			return null;
		}

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);

		return calendar;
	}

	public TTimeSceneData getTimeSceneData() {
		TTimeSceneData data = TTimeSceneData.Factory.newInstance();
		data.setIndex(id);

		// begin time

		if (begintime != null) {
			Calendar c = getTimeByText(begintime);
			if (c != null) {
				data.setStartTime(c);
			}
		}

		// end time.
		if (endtime != null) {
			Calendar c = getTimeByText(endtime);
			if (c != null) {
				data.setEndTime(c);
			}
		}

		// classes
		TTimeSceneClasses.Enum classes = null;
		if ("每天".equals(detailrepeat)) {
			classes = TTimeSceneClasses.EVERYDAY;
		} else if ("单次".equals(detailrepeat)) {
			classes = TTimeSceneClasses.ONCE;
		} else if (detailrepeat != null) {
			String tmp[] = detailrepeat.split("-");
			if (tmp.length == 2) {
				Integer weekStart = null;
				Integer weekEnd = null;
				for (int i=0; i<DataResours.datas.length; i++) {
					if (DataResours.datas[i].equals(tmp[0])) {
						if (i == 6) {
							weekStart = 1;
						} else {
							weekStart = i + 2;
						}
					}
					
					if (DataResours.datas[i].equals(tmp[1])) {
						if (i == 6) {
							weekEnd = 1;
						} else {
							weekEnd = i + 2;
						}
					}
					
					if (weekStart != null && weekEnd != null) {
						classes = TTimeSceneClasses.WEEKLY;
						data.setWeekStart(weekStart);
						data.setWeekEnd(weekEnd);
						break;
					}
				}
			}
		}

		if (classes != null) {
			data.setClasses(classes);
		}
		
		// policy
		if (DataResours.status[0].equals(timesetting_status)) {
			data.setPolicy(TCallPolicyState.SM);

			// set the scene
			switch (timesetting_icon_id) {
			case 0:
				data.setScene(TScenesState.XX);
				break;
			case 1:
				data.setScene(TScenesState.KC);
				break;
			case 2:
				data.setScene(TScenesState.KH);
				break;
			case 3:
				data.setScene(TScenesState.SK);
				break;
			case 4:
				data.setScene(TScenesState.CG);
				break;
			case 5:
				data.setScene(TScenesState.TY);
				break;
			default:
				break;
			}
		} else if (DataResours.status[1].equals(timesetting_status)) {
			data.setPolicy(TCallPolicyState.GJ);

			// set the scene.
			switch (timesetting_icon_id) {
			case 0:
				data.setScene(TScenesState.XX);
				break;
			case 1:
				data.setScene(TScenesState.KC);
				break;
			case 2:
				data.setScene(TScenesState.KH);
				break;
			case 3:
				data.setScene(TScenesState.SK);
				 break;
			case 4:
				data.setScene(TScenesState.FJ);
				break;
			case 5:
				data.setScene(TScenesState.GJ_TSY);
				break;
			case 6:
				data.setScene(TScenesState.OOS);
				break;
			case 7:
				data.setScene(TScenesState.CG);
				break;
			case 8:
				data.setScene(TScenesState.TY);
				break;
			default:
				break;
			}
		}
		
		return data;
	}


	public int getTimesetting_icon_id() {
	return timesetting_icon_id;
}
public void setTimesetting_icon_id(int timesettingIconId) {
	timesetting_icon_id = timesettingIconId;
}
	public int getTimesetting_isopen() {
	return timesetting_isopen;
}
	public void setTimesetting_isopen(int timesettingIsopen) {
		timesetting_isopen = timesettingIsopen;
	}
	public String getDetailrepeat() {
		return detailrepeat;
	}
	public void setDetailrepeat(String detailrepeat) {
		this.detailrepeat = detailrepeat;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBegintime() {
		return begintime;
	}
	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getTimesetting_status() {
		return timesetting_status;
	}
	public void setTimesetting_status(String timesettingStatus) {
		timesetting_status = timesettingStatus;
	}
	public String getTimesetting_scene() {
		return timesetting_scene;
	}
	public void setTimesetting_scene(String timesettingScene) {
		timesetting_scene = timesettingScene;
	}
	   
}
