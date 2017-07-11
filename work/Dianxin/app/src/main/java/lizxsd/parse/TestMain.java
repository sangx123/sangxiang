/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: Jun 9, 2011.
 * Create Description
 *
 *  $Id: Common.java,v 1.2 2011/06/12 06:36:31 zhm Exp $
 *
 ************************* To  Do ***********************************
 *
 ************************* Others ***********************************
 * Add anything you want to write here.
 * 
 * 
 ******************************************************************
 */
package lizxsd.parse;

import java.util.Calendar;
import java.util.Vector;

import com.liz.cptgen.TCallPolicyState;
import com.liz.cptgen.TScenesState;
import com.liz.cptr.CptRPacketParse;
import com.liz.cptr.TBlackwhiteState;
import com.liz.cptr.TDurationSceneData;
import com.liz.cptr.TGetDurationStateRsp;
import com.liz.cptr.TGetTimeSceneResult;
import com.liz.cptr.TGetTimeSceneRsp;
import com.liz.cptr.TPhonebookReturn;
import com.liz.cptr.TSetPhonebooksReq;
import com.liz.cptr.TTimeSceneClasses;
import com.liz.cptr.TTimeSceneData;

/**
 * The andriod client command file.
 * 
 * ≤‚ ‘
 */
public class TestMain {
	public final static Integer ANDROID_CLIENT_TYPE = 9;

	public static void testTPhoneBook() throws Exception {
		TSetPhonebooksReq request = TSetPhonebooksReq.Factory.newInstance();

		request.setSeqId(100);
		request.setForceUpdate(false);
		request.setOriginalNumber("324234234");
		request.setMsisdn("13800010005");

		TPhonebookReturn phonebooks = request.addNewInfo();
		phonebooks.setPbId(5);
		phonebooks.setPhoneNumber("13800010002");
		phonebooks.setState(TBlackwhiteState.BLACKLIST);
		phonebooks.setUserName("zhou.h.m");
		phonebooks.setComment("this is the comment");

		request.setInfo(phonebooks);

		String afterEncoder = CptRPacketParse.encode(request);
		
		System.out.println(afterEncoder);

		TSetPhonebooksReq myRequest = (TSetPhonebooksReq) CptRPacketParse.parse(afterEncoder);

		System.out.println(myRequest);
	}

	public static void testSceneData() throws Exception {
		TGetDurationStateRsp response = TGetDurationStateRsp.Factory.newInstance();
		response.setSeqId(112);
		TDurationSceneData data = response.addNewData();
		data.setDurations(123);
		data.setStartTime(Calendar.getInstance());
		data.setScene(TScenesState.CG);
		data.setPolicy(TCallPolicyState.GJ);

		String afterEncoder = CptRPacketParse.encode(response);

		System.out.println(afterEncoder);

		TGetDurationStateRsp myRequest = (TGetDurationStateRsp) CptRPacketParse.parse(afterEncoder);

		System.out.println(myRequest);

	}

	public static void testTimeData() throws Exception {
		TGetTimeSceneRsp response = TGetTimeSceneRsp.Factory.newInstance();
		response.setSeqId(34234);
		response.setResult(TGetTimeSceneResult.FAILED_NO_DATA_AVAIABLE);
		Vector<TTimeSceneData> datas = response.addNewData();

		for (int i = 0; i < 5; i++) {
			TTimeSceneData data = TTimeSceneData.Factory.newInstance();
			data.setClasses(TTimeSceneClasses.WEEKLY);
			data.setWeekStart(1);
			data.setWeekEnd(7);
			data.setIndex(i);
			data.setPolicy(TCallPolicyState.GJ);
			data.setPrority(i);
			data.setStartTime(Calendar.getInstance());
			data.setEndTime(Calendar.getInstance());
			datas.add(data);
		}

		String afterEncoder = CptRPacketParse.encode(response);

		System.out.println(afterEncoder);

		TGetTimeSceneRsp myRequest = (TGetTimeSceneRsp) CptRPacketParse.parse(afterEncoder);

		System.out.println(myRequest);

	}

	public static void main(String[] args) throws Exception {
		try {
			testTimeData();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
}
