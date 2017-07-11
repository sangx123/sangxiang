package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TDurationSceneData implements IPacket { 

	com.liz.cptgen.TCallPolicyState.Enum policy;
	com.liz.cptgen.TScenesState.Enum scene = null;
	java.util.Calendar startTime = null;
	Integer durations = null;

	final private static String POLICY = "policy";
	final private static String SCENE = "scene";
	final private static String STARTTIME = "startTime";
	final private static String DURATIONS = "durations";


	private TDurationSceneData() {

	}

	public static class Factory {
		static public TDurationSceneData newInstance() {
			return new TDurationSceneData();
		}
	}


	static public TDurationSceneData parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TDurationSceneData packet = new TDurationSceneData();
		String name;
		
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
		if (name.equals(STARTTIME)) {
			packet.startTime = SimpleXmlParse.parseDateTime(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(DURATIONS)) {
			packet.durations = Integer.valueOf(parse.takeValue());
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
		
		SimpleXmlParse.appendXmlStart(buffer, POLICY);
		buffer.append(policy);
		SimpleXmlParse.appendXmlEnd(buffer, POLICY);

		if (scene != null) {
			SimpleXmlParse.appendXmlStart(buffer, SCENE);
			buffer.append(scene);
			SimpleXmlParse.appendXmlEnd(buffer, SCENE);
		}

		if (startTime != null) {
			SimpleXmlParse.appendXmlStart(buffer, STARTTIME);
			SimpleXmlParse.encodeDateTime(startTime, buffer);
			SimpleXmlParse.appendXmlEnd(buffer, STARTTIME);
		}

		if (durations != null) {
			SimpleXmlParse.appendXmlStart(buffer, DURATIONS);
			buffer.append(durations);
			SimpleXmlParse.appendXmlEnd(buffer, DURATIONS);
		}

		
	}
	
	public String getXmlTagName() {
		return "t_duration_scene_data";
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

	public java.util.Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(java.util.Calendar startTime) {
		this.startTime = startTime;
	}

	public boolean isSetStartTime() {
		return (this.startTime != null);
	}

	public Integer getDurations() {
		return durations;
	}

	public void setDurations(Integer durations) {
		this.durations = durations;
	}

	public boolean isSetDurations() {
		return (this.durations != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

