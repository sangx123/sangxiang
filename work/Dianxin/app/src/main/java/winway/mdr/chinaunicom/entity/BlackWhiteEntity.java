package winway.mdr.chinaunicom.entity;
/*****************************
 * 黑白名单实体类
 * @author zhaohao
 * 时间:2011-11
 */
public class BlackWhiteEntity {
   private int id;
   private String phone_name;
   private String phone_number;
   private String phone_beizhu;
   private int black_or_white;
   public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getPhone_name() {
	return phone_name;
}
public void setPhone_name(String phoneName) {
	phone_name = phoneName;
}
public String getPhone_number() {
	return phone_number;
}
public void setPhone_number(String phoneNumber) {
	phone_number = phoneNumber;
}
public String getPhone_beizhu() {
	return phone_beizhu;
}
public void setPhone_beizhu(String phoneBeizhu) {
	phone_beizhu = phoneBeizhu;
}
public int getBlack_or_white() {
	return black_or_white;
}
public void setBlack_or_white(int blackOrWhite) {
	black_or_white = blackOrWhite;
}

}
