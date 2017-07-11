/**
 * 
 */
package winway.mdr.chinaunicom.comm;
/**
 * 
 * @author �Ժ� 
 * E-mail:yuafen821@126.com
 * @version ����ʱ�䣺2012-6-18 ����02:54:12
 * ��˵��
 */
public class StringUnits {
	/**
	 * @description ��ȡһ���ַ������ַ�������������Ӣ�ģ�һ��������2���ַ���
	 * @param content
	 * @return
	 */
	public static int getCharacterNum(final String content) {
	if (null == content || "".equals(content)) {
	return 0;
	}else {
	return (content.length() + getChineseNum(content));
	}
	}
	/**
	* @description �����ַ����������ֻ���ȫ���ַ��ĸ���
	* @param s
	* @return
	*/
	public static int getChineseNum(String s) {
	 
	int num = 0;
	char[] myChar = s.toCharArray();
	for (int i = 0; i < myChar.length; i++) {
	if ((char)(byte)myChar[i] != myChar[i]) {
	num++;
	}
	}
	return num;
	}
	 
}
