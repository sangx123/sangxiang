
package lizxsd.parse;


/**
 * 数据类型
 */
public abstract class SeqidPacket implements IPacket {

	protected int seqId = 0;
	
	final protected static String SEQ_ID = "seqid";
	
	/** @return Returns the seqId.
	 */
	public int getSeqId() {
		return seqId;
	}

	/** @param seqId The seqId to set.
	 */
	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}
	
	abstract  public String getXmlTagName();
	
	abstract public void encode(StringBuffer buffer);
}
