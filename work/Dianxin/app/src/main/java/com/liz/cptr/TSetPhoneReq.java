package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

/**
 * Created by sangxiang on 12/7/17.
 */

public class TSetPhoneReq extends SeqidPacket {
    Integer clientType = 2;
    String msisdn;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    String phone;

    final private static String CLIENT_TYPE = "client_type";
    final private static String MSISDN = "msisdn";
    final private static String PHONE = "dj_phone_number";


    private TSetPhoneReq() {

    }

    public static class Factory {
        static public TSetPhoneReq newInstance() {
            return new TSetPhoneReq();
        }
    }


    static public TSetPhoneReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{

        TSetPhoneReq packet = new TSetPhoneReq();
        String name;

        name = parse.takeName();
        if (!name.equals(SEQ_ID)) {
            throw new XmlParseException(SEQ_ID, name);
        }
        packet.seqId = Integer.parseInt(parse.takeValue());

        name = parse.takeName();
        if (name.equals(CLIENT_TYPE)) {
            packet.clientType = Integer.valueOf(parse.takeValue());
        } else {
            parse.takePreviousName();
        }

        name = parse.takeName();
        if (!name.equals(MSISDN)) {
            throw new XmlParseException(MSISDN, name);
        }
        packet.msisdn = parse.takeValue();

        name = parse.takeName();
        if (!name.equals(PHONE)) {
            throw new XmlParseException(PHONE, name);
        }
        packet.phone = parse.takeValue();
        parse.takeName();
        return packet;
    }
    /**
     */
    public void encode(StringBuffer buffer) {

        SimpleXmlParse.appendXmlStart(buffer, SEQ_ID);
        buffer.append(seqId);
        SimpleXmlParse.appendXmlEnd(buffer, SEQ_ID);

        if (clientType != null) {
            SimpleXmlParse.appendXmlStart(buffer, CLIENT_TYPE);
            buffer.append(clientType);
            SimpleXmlParse.appendXmlEnd(buffer, CLIENT_TYPE);
        }

        SimpleXmlParse.appendXmlStart(buffer, MSISDN);
        buffer.append(msisdn);
        SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

        SimpleXmlParse.appendXmlStart(buffer, PHONE);
        buffer.append(phone);
        SimpleXmlParse.appendXmlEnd(buffer, PHONE);


    }

    public String getXmlTagName() {
        return CptRPacketParse.SetPhone;
    }
    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    public boolean isSetClientType() {
        return (this.clientType != null);
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }




    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        encode(buffer);
        return buffer.toString();
    }
}
