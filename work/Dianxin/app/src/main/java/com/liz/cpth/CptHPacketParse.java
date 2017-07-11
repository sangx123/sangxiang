package com.liz.cpth;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
public class CptHPacketParse {

	//the name defines.
	public static final String CHECK_VERSION_REQ = "check_version_req";
	public static final String CHECK_VERSION_RSP = "check_version_rsp";
	public static final String ADD_USER_OPINION_REQ = "add_user_opinion_req";
	public static final String ADD_USER_OPINION_RSP = "add_user_opinion_rsp";
	public static final String GET_SAC_URL_BY_MSISDN_REQ = "get_sac_url_by_msisdn_req";
	public static final String GET_SAC_URL_BY_MSISDN_RSP = "get_sac_url_by_msisdn_rsp";

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
		if (name.equals(CHECK_VERSION_REQ)) {
			return com.liz.cpth.TCheckVersionReq.parse(parse);
		}
		else if (name.equals(CHECK_VERSION_RSP)) {
			return com.liz.cpth.TCheckVersionRsp.parse(parse);
		}
		else if (name.equals(ADD_USER_OPINION_REQ)) {
			return com.liz.cpth.TAddUserOpinionReq.parse(parse);
		}
		else if (name.equals(ADD_USER_OPINION_RSP)) {
			return com.liz.cpth.TAddUserOpinionRsp.parse(parse);
		}
		else if (name.equals(GET_SAC_URL_BY_MSISDN_REQ)) {
			return com.liz.cpth.TGetSacUrlByMsisdnReq.parse(parse);
		}
		else if (name.equals(GET_SAC_URL_BY_MSISDN_RSP)) {
			return com.liz.cpth.TGetSacUrlByMsisdnRsp.parse(parse);
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
		buffer.append("<cpth:cpt_h xmlns:cpth=\"http://www.liz.com/cpth\">");
		
		SimpleXmlParse.appendXmlStart(buffer, packet.getXmlTagName());
		packet.encode(buffer);
		SimpleXmlParse.appendXmlEnd(buffer, packet.getXmlTagName());
		
		//append end.
		buffer.append("</cpth:cpt_h>");
		
		return buffer.toString();
	}
}
