package com.navi.client;

public interface Config {
	static final int BASE = 7000;
	public static final int REQUEST_LOGIN = BASE + 1;
	public static final int REQUEST_REGISTER = BASE + 2;
	public static final int REQUEST_EXIT = BASE + 3;
	public static final int REQUEST_DOWNLOAD = BASE + 4;
	public static final int REQUEST_PATHINFO = BASE + 5;
	public static final int REQUEST_SENDREQUEST = BASE + 6;
	public static final int REQUEST_SETREQUESTINFO = BASE + 7;
	public static final int REQUEST_SAVEHISTORY = BASE + 8;
	public static final int REQUEST_GATHISTORY = BASE + 9;
	
	public static final int CON_SUCCESS = 2002;
	public static final int SUCCESS = 2000;  
	public static final int FAIl = 2001;    
	
	public static final int USER_STATE_ONLINE = 3000;  
	public static final int USER_STATE_NON_ONLINE = 3001; 
	
	public static final String RESULT = "result";
	public static final String REQUEST_TYPE = "requestType";
	
	public static final int ACK_OPEN_ROUTE = 100;

	public static final int ACK_SAY_START = 101;
	
	public static final int ACK_SAY_END = 102;	
	
	public static final int ACK_BEFORE_ROUTE = 103;	
	
	public static final int ACK_START_ROUTE = 107;		

	public static final int ACK_SAY_END_RESULT = 104;		
	
	public static final int ACK_LISTEN_START = 105;		

	public static final int ACK_LISTEN_END = 106;	
	
	public static final int ACK_FACE_RKN_EXIT = 108;	

	public static final int ACK_NONE = 109;	

	public static final int ACK_ROUTE_RETURN = 110;	
	
	public static final int ACK_FACE_RKN = 111;	

	public static final int ACK_FACE_MODEL = 112;

	public static final int ACK_MAIN_WELCOME = 113;	
	
	public static final int ACK_NAVI_START = 114;		
	
	public static final int ACK_ROUTE_START = 115;

	public static final int ACK_BEFORE_NAVI = 116;
	
	public static final int ACK_START_NAVI = 117;

	public static final int ACK_PARSE_LL = 118;
	
	public static final int ACK_READ_END = 119;

	public static final int ACK_SERVICE = 120;
	
	public static final int REQ_LOC = 121; 
	
	public static final int RES_LOC = 122; 
	
	public static final int INTENT_LOC = 123;
	
	public static final String TAG = "lanlan";
	
	public static final int REQ_SM = 124;
	
	public static final int RES_SM = 125;
	
	public static final int ACK_BLUE_SUCCESS = 126;
	
	public static final int ACK_BLUE_CON_SUCCESS = 127;

	public static final int ACK_END_POINT = 128;

	public static final int NONEPLACE = 129;
	
	public static final int ACK_VOICE_SERVICE = 130;
	
	public static final int ACK_FINDPATH_SUCCESS = 131;
	
	public static final int ACK_CHECKPOINT_FAIL = 132;
	
	public static final int ACK_FINDPATH_FAIL = 133;
	
	public static final int ACK_BLUETOOTH_FAIL = 134;

	
	
}
