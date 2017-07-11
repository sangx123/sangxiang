package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
public class CptRPacketParse {

	//the name defines.
	public static final String SET_PHONEBOOKS_REQ = "set_phonebooks_req";
	public static final String SET_PHONEBOOKS_RSP = "set_phonebooks_rsp";
	public static final String DEL_PHONEBOOKS_REQ = "del_phonebooks_req";
	public static final String DEL_PHONEBOOKS_RSP = "del_phonebooks_rsp";
	public static final String GET_PHONEBOOKS_REQ = "get_phonebooks_req";
	public static final String GET_PHONEBOOKS_RSP = "get_phonebooks_rsp";
	public static final String USER_LOGIN_REQ = "user_login_req";
	public static final String USER_LOGIN_RSP = "user_login_rsp";
	public static final String SET_CALLED_STATE_ENABLE_REQ = "set_called_state_enable_req";
	public static final String SET_CALLED_STATE_ENABLE_RSP = "set_called_state_enable_rsp";
	public static final String GET_CALLED_STATE_ENABLE_REQ = "get_called_state_enable_req";
	public static final String GET_CALLED_STATE_ENABLE_RSP = "get_called_state_enable_rsp";
	public static final String SET_SMS_INFO_REQ = "set_sms_info_req";
	public static final String SET_SMS_INFO_RSP = "set_sms_info_rsp";
	public static final String GET_SMS_INFO_REQ = "get_sms_info_req";
	public static final String GET_SMS_INFO_RSP = "get_sms_info_rsp";
	public static final String SET_DURATION_SCENE_REQ = "set_duration_scene_req";
	public static final String SET_DURATION_SCENE_RSP = "set_duration_scene_rsp";
	public static final String GET_DURATION_STATE_REQ = "get_duration_state_req";
	public static final String GET_DURATION_STATE_RSP = "get_duration_state_rsp";
	public static final String ADD_TIME_SCENE_REQ = "add_time_scene_req";
	public static final String ADD_TIME_SCENE_RSP = "add_time_scene_rsp";
	public static final String REPLACE_TIME_SCENE_REQ = "replace_time_scene_req";
	public static final String REPLACE_TIME_SCENE_RSP = "replace_time_scene_rsp";
	public static final String GET_TIME_SCENE_REQ = "get_time_scene_req";
	public static final String GET_TIME_SCENE_RSP = "get_time_scene_rsp";
	public static final String GET_SINGLE_OPTION_REQ = "get_single_option_req";
	public static final String GET_SINGLE_OPTION_RSP = "get_single_option_rsp";
	public static final String SET_SINGLE_OPTION_REQ = "set_single_option_req";
	public static final String SET_SINGLE_OPTION_RSP = "set_single_option_rsp";
	public static final String GET_MULTIPLE_OPTIONS_REQ = "get_multiple_options_req";
	public static final String GET_MULTIPLE_OPTIONS_RSP = "get_multiple_options_rsp";
	public static final String SET_MULTIPLE_OPTIONS_REQ = "set_multiple_options_req";
	public static final String SET_MULTIPLE_OPTIONS_RSP = "set_multiple_options_rsp";
	public static final String CHANGE_PWD_REQ="change_pwd_req";
	public static final String CHANGE_PWD_RSP="change_pwd_rsp";

	/**
	 * parse the packet
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static IPacket parse(String text) throws Exception {
		SimpleXmlParse parse = new SimpleXmlParse(text);
		
		//take the text like: <cptr:cpt_r xmlns:cptr="http://www.liz.com/cptr">
		parse.takeName();
		
		//take the name.
		String name = parse.takeName();
		if (name.equals(SET_PHONEBOOKS_REQ)) {
			return com.liz.cptr.TSetPhonebooksReq.parse(parse);
		}
		else if (name.equals(SET_PHONEBOOKS_RSP)) {
			return com.liz.cptr.TSetPhonebooksRsp.parse(parse);
		}
		else if (name.equals(DEL_PHONEBOOKS_REQ)) {
			return com.liz.cptr.TDelPhonebooksReq.parse(parse);
		}
		else if (name.equals(DEL_PHONEBOOKS_RSP)) {
			return com.liz.cptr.TDelPhonebooksRsp.parse(parse);
		}
		else if (name.equals(GET_PHONEBOOKS_REQ)) {
			return com.liz.cptr.TGetPhonebooksReq.parse(parse);
		}
		else if (name.equals(GET_PHONEBOOKS_RSP)) {
			return com.liz.cptr.TGetPhonebooksRsp.parse(parse);
		}
		else if (name.equals(USER_LOGIN_REQ)) {
			return com.liz.cptr.TUserLoginReq.parse(parse);
		}
		else if (name.equals(USER_LOGIN_RSP)) {
			return com.liz.cptr.TUserLoginRsp.parse(parse);
		}
		else if (name.equals(SET_CALLED_STATE_ENABLE_REQ)) {
			return com.liz.cptr.TSetCalledStateEnableReq.parse(parse);
		}
		else if (name.equals(SET_CALLED_STATE_ENABLE_RSP)) {
			return com.liz.cptr.TSetCalledStateEnableRsp.parse(parse);
		}
		else if (name.equals(GET_CALLED_STATE_ENABLE_REQ)) {
			return com.liz.cptr.TGetCalledStateEnableReq.parse(parse);
		}
		else if (name.equals(GET_CALLED_STATE_ENABLE_RSP)) {
			return com.liz.cptr.TGetCalledStateEnableRsp.parse(parse);
		}
		else if (name.equals(SET_SMS_INFO_REQ)) {
			return com.liz.cptr.TSetSmsInfoReq.parse(parse);
		}
		else if (name.equals(SET_SMS_INFO_RSP)) {
			return com.liz.cptr.TSetSmsInfoRsp.parse(parse);
		}
		else if (name.equals(GET_SMS_INFO_REQ)) {
			return com.liz.cptr.TGetSmsInfoReq.parse(parse);
		}
		else if (name.equals(GET_SMS_INFO_RSP)) {
			return com.liz.cptr.TGetSmsInfoRsp.parse(parse);
		}
		else if (name.equals(SET_DURATION_SCENE_REQ)) {
			return com.liz.cptr.TSetDurationSceneReq.parse(parse);
		}
		else if (name.equals(SET_DURATION_SCENE_RSP)) {
			return com.liz.cptr.TSetDurationSceneRsp.parse(parse);
		}
		else if (name.equals(GET_DURATION_STATE_REQ)) {
			return com.liz.cptr.TGetDurationStateReq.parse(parse);
		}
		else if (name.equals(GET_DURATION_STATE_RSP)) {
			return com.liz.cptr.TGetDurationStateRsp.parse(parse);
		}
		else if (name.equals(ADD_TIME_SCENE_REQ)) {
			return com.liz.cptr.TAddTimeSceneReq.parse(parse);
		}
		else if (name.equals(ADD_TIME_SCENE_RSP)) {
			return com.liz.cptr.TAddTimeSceneRsp.parse(parse);
		}
		else if (name.equals(REPLACE_TIME_SCENE_REQ)) {
			return com.liz.cptr.TReplaceTimeSceneReq.parse(parse);
		}
		else if (name.equals(REPLACE_TIME_SCENE_RSP)) {
			return com.liz.cptr.TReplaceTimeSceneRsp.parse(parse);
		}
		else if (name.equals(GET_TIME_SCENE_REQ)) {
			return com.liz.cptr.TGetTimeSceneReq.parse(parse);
		}
		else if (name.equals(GET_TIME_SCENE_RSP)) {
			return com.liz.cptr.TGetTimeSceneRsp.parse(parse);
		}
		else if (name.equals(GET_SINGLE_OPTION_REQ)) {
			return com.liz.cptr.TGetSingleOptionReq.parse(parse);
		}
		else if (name.equals(GET_SINGLE_OPTION_RSP)) {
			return com.liz.cptr.TGetSingleOptionRsp.parse(parse);
		}
		else if (name.equals(SET_SINGLE_OPTION_REQ)) {
			return com.liz.cptr.TSetSingleOptionReq.parse(parse);
		}
		else if (name.equals(SET_SINGLE_OPTION_RSP)) {
			return com.liz.cptr.TSetSingleOptionRsp.parse(parse);
		}
		else if (name.equals(GET_MULTIPLE_OPTIONS_REQ)) {
			return com.liz.cptr.TGetMultipleOptionsReq.parse(parse);
		}
		else if (name.equals(GET_MULTIPLE_OPTIONS_RSP)) {
			return com.liz.cptr.TGetMultipleOptionsRsp.parse(parse);
		}
		else if (name.equals(SET_MULTIPLE_OPTIONS_REQ)) {
			return com.liz.cptr.TSetMultipleOptionsReq.parse(parse);
		}
		else if (name.equals(SET_MULTIPLE_OPTIONS_RSP)) {
			return com.liz.cptr.TSetMultipleOptionsRsp.parse(parse);
		}else if(name.equals(CHANGE_PWD_RSP)){
			return TEditPasswordRsp.parse(parse);
		}

		return null;

	}
	
	/**
	 * encode the packet
	 * @param packet
	 * @return
	 */
	public static String encode(IPacket packet) {
		StringBuffer buffer = new StringBuffer();
		
		//append start
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<cptr:cpt_r xmlns:cptr=\"http://www.liz.com/cptr\">");
		
		SimpleXmlParse.appendXmlStart(buffer, packet.getXmlTagName());
		packet.encode(buffer);
		SimpleXmlParse.appendXmlEnd(buffer, packet.getXmlTagName());
		
		//append end.
		buffer.append("</cptr:cpt_r>");
		
		return buffer.toString();
	}
}
