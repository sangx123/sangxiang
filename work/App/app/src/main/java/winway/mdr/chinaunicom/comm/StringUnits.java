/**
 * 
 */
package winway.mdr.chinaunicom.comm;
/**
 * 
 * @author 赵豪 
 * E-mail:yuafen821@126.com
 * @version 创建时间：2012-6-18 下午02:54:12
 * 类说明
 */
public class StringUnits {
	/**
	 * @description 获取一段字符串的字符个数（包含中英文，一个中文算2个字符）
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
	* @description 返回字符串里中文字或者全角字符的个数
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
