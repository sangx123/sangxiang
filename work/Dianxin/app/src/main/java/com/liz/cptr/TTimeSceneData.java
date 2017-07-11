package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TTimeSceneData implements IPacket { 

	Integer index = null;
	Integer prority = null;
	com.liz.cptr.TTimeSceneClasses.Enum classes;
	com.liz.cptgen.TCallPolicyState.Enum policy;
	com.liz.cptgen.TScenesState.Enum scene = null;
	Integer weekStart = null;
	Integer weekEnd = null;
	java.util.Calendar startTime;
	java.util.Calendar endTime;
	java.util.Calendar addTime = null;

	final private static String INDEX = "index";
	final private static String PRORITY = "prority";
	final private static String CLASSES = "classes";
	final private static String POLICY = "policy";
	final private static String SCENE = "scene";
	final private static String WEEK_START = "week_start";
	final private static String WEEK_END = "week_end";
	final private static String START_TIME = "start_time";
	final private static String END_TIME = "end_time";
	final private static String ADD_TIME = "add_time";


	private TTimeSceneData() {

	}

	public static class Factory {
		static public TTimeSceneData newInstance() {
			return new TTimeSceneData();
		}
	}


	static public TTimeSceneData parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TTimeSceneData packet = new TTimeSceneData();
		String name;
		
		name = parse.takeName();
		if (name.equals(INDEX)) {
			packet.index = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(PRORITY)) {
			packet.prority = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(CLASSES)) {
			throw new XmlParseException(CLASSES, name);
		}
		packet.classes = com.liz.cptr.TTimeSceneClasses.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(POLICY)) {
			throw new XmlParseException(POLICY, name);
		}
		packet.policy = com.liz.cptgen.TCallPolicyState.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(SCENE)) {
			packet.scene = com.liz.cptgen.TScenesState.Enum.parse(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(WEEK_START)) {
			packet.weekStart = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(WEEK_END)) {
			packet.weekEnd = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(START_TIME)) {
			throw new XmlParseException(START_TIME, name);
		}
		packet.startTime = SimpleXmlParse.parseTime(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(END_TIME)) {
			throw new XmlParseException(END_TIME, name);
		}
		packet.endTime = SimpleXmlParse.parseTime(parse.takeValue());

		name = parse.takeName();
		if (name.equals(ADD_TIME)) {
			packet.addTime = SimpleXmlParse.parseDateTime(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		if (index != null) {
			SimpleXmlParse.appendXmlStart(buffer, INDEX);
			buffer.append(index);
			SimpleXmlParse.appendXmlEnd(buffer, INDEX);
		}

		if (prority != null) {
			SimpleXmlParse.appendXmlStart(buffer, PRORITY);
			buffer.append(prority);
			SimpleXmlParse.appendXmlEnd(buffer, PRORITY);
		}

		SimpleXmlParse.appendXmlStart(buffer, CLASSES);
		buffer.append(classes);
		SimpleXmlParse.appendXmlEnd(buffer, CLASSES);

		SimpleXmlParse.appendXmlStart(buffer, POLICY);
		buffer.append(policy);
		SimpleXmlParse.appendXmlEnd(buffer, POLICY);

		if (scene != null) {
			SimpleXmlParse.appendXmlStart(buffer, SCENE);
			buffer.append(scene);
			SimpleXmlParse.appendXmlEnd(buffer, SCENE);
		}

		if (weekStart != null) {
			SimpleXmlParse.appendXmlStart(buffer, WEEK_START);
			buffer.append(weekStart);
			SimpleXmlParse.appendXmlEnd(buffer, WEEK_START);
		}

		if (weekEnd != null) {
			SimpleXmlParse.appendXmlStart(buffer, WEEK_END);
			buffer.append(weekEnd);
			SimpleXmlParse.appendXmlEnd(buffer, WEEK_END);
		}

		SimpleXmlParse.appendXmlStart(buffer, START_TIME);
		SimpleXmlParse.encodeTime(startTime, buffer);
		SimpleXmlParse.appendXmlEnd(buffer, START_TIME);

		SimpleXmlParse.appendXmlStart(buffer, END_TIME);
		SimpleXmlParse.encodeTime(endTime, buffer);
		SimpleXmlParse.appendXmlEnd(buffer, END_TIME);

		if (addTime != null) {
			SimpleXmlParse.appendXmlStart(buffer, ADD_TIME);
			SimpleXmlParse.encodeDateTime(addTime, buffer);
			SimpleXmlParse.appendXmlEnd(buffer, ADD_TIME);
		}

		
	}
	
	public String getXmlTagName() {
		return "t_time_scene_data";
	}
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public boolean isSetIndex() {
		return (this.index != null);
	}

	public Integer getPrority() {
		return prority;
	}

	public void setPrority(Integer prority) {
		this.prority = prority;
	}

	public boolean isSetPrority() {
		return (this.prority != null);
	}

	public com.liz.cptr.TTimeSceneClasses.Enum getClasses() {
		return classes;
	}

	public void setClasses(com.liz.cptr.TTimeSceneClasses.Enum classes) {
		this.classes = classes;
	}

	public com.liz.cptgen.TCallPolicyState.Enum getPolicy() {
		return policy;
	}

	public void setPolicy(com.liz.cptgen.TCallPolicyState.Enum policy) {
		this.policy = policy;
	}

	public com.liz.cptgen.TScenesState.Enum getScene() {
		return scene;
	}

	public void setScene(com.liz.cptgen.TScenesState.Enum scene) {
		this.scene = scene;
	}

	public boolean isSetScene() {
		return (this.scene != null);
	}

	public Integer getWeekStart() {
		return weekStart;
	}

	public void setWeekStart(Integer weekStart) {
		this.weekStart = weekStart;
	}

	public boolean isSetWeekStart() {
		return (this.weekStart != null);
	}

	public Integer getWeekEnd() {
		return weekEnd;
	}

	public void setWeekEnd(Integer weekEnd) {
		this.weekEnd = weekEnd;
	}

	public boolean isSetWeekEnd() {
		return (this.weekEnd != null);
	}

	public java.util.Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(java.util.Calendar startTime) {
		this.startTime = startTime;
	}

	public java.util.Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(java.util.Calendar endTime) {
		this.endTime = endTime;
	}

	public java.util.Calendar getAddTime() {
		return addTime;
	}

	public void setAddTime(java.util.Calendar addTime) {
		this.addTime = addTime;
	}

	public boolean isSetAddTime() {
		return (this.addTime != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

