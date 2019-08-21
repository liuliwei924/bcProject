package org.bc.admin.util.constant;

public enum PushType {

	FUMI(1,"福米金融");

	private int pushType;
	
	private String pushTypeName;
	
	private PushType(int pushType,String pushTypeName){
		this.pushType = pushType;
		this.pushTypeName = pushTypeName;
	}
	
	public int getPushType() {
		return pushType;
	}

	public void setPushType(int pushType) {
		this.pushType = pushType;
	}

	public String getPushTypeName() {
		return pushTypeName;
	}

	public void setPushTypeName(String pushTypeName) {
		this.pushTypeName = pushTypeName;
	}
}
