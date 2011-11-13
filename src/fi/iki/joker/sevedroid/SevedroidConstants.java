package fi.iki.joker.sevedroid;

public class SevedroidConstants {
	
	protected static final String APP_VERSION = "0.5beta";
	protected static final String HTTPCLIENT_USERAGENT = "Sevedroid Android Application v."+APP_VERSION;
	
	protected static final String API_KEY_SUBSTR = "%API_KEY_HERE%";
	protected static final String SOAP_BODY_SUBSTR = "%BODY_CONTENT_HERE%";
	
	protected static final String S3_WSDL_URL = "https://sync.severa.com/webservice/S3/API.svc/WSDL";
	protected static final String S3_API_URL = "https://sync.severa.com/webservice/S3/API.svc";
	//protected static final String S3_API_URL = "http://192.168.0.2:7000";

	protected static final int CODE_INTERNAL_SERVER_ERROR = 500;
	
	protected static final String SOAP_ACTION_GET_USER_BY_NAME = "http://soap.severa.com/IUser/GetUserByName";
	protected static final String SOAP_ACTION_GET_ALL_CASES = "http://soap.severa.com/ICase/GetAllCases";
	protected static final String SOAP_ACTION_GET_PHASES_BY_CASE_GUID = "http://soap.severa.com/IPhase/GetPhasesByCaseGUID";
	protected static final String SOAP_ACTION_GET_WORKTYPES_BY_PHASE_GUID = "http://soap.severa.com/IWorkType/GetWorkTypesByPhaseGUID";
	protected static final String SOAP_ACTION_PUBLISH_HOURENTRY = "http://soap.severa.com/IHourEntry/AddNewHourEntry";
	
	protected static final String SOAP_AUTHN_ENVELOPE = 
			"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sev=\"http://schemas.datacontract.org/2004/07/Severa.Entities.API\" xmlns:ns1 = \"http://soap.severa.com/\">"+
			"<SOAP-ENV:Header>"+
				"<ns1:WebServicePassword>"+API_KEY_SUBSTR+"</ns1:WebServicePassword>"+
			"</SOAP-ENV:Header>"+
			"<SOAP-ENV:Body>"+
			SOAP_BODY_SUBSTR+
			"</SOAP-ENV:Body>"+
			"</SOAP-ENV:Envelope>";
	
	/**
	 * This is the soap message body to retrieve activities from the api
	 * The date format wanted by the api is "YYYY-MM-DD" (i.e. "2011-01-01")
	 */
	protected static final String GET_ACTIVITIES_BODY = "<soap:GetActivityInstances>"+
         "<soap:userGUID></soap:userGUID>"+
         "<soap:startsAfter>2011-01-01</soap:startsAfter>"+
         "<soap:startsBefore>2011-10-06</soap:startsBefore>"+
         "<soap:endsAfter>2011-01-01</soap:endsAfter>"+
         "<soap:endsBefore>2012-01-01</soap:endsBefore>"+
         "<soap:activityTypeGUID></soap:activityTypeGUID>"+
         "<soap:accountGUID></soap:accountGUID>"+         
         "<soap:caseGUID></soap:caseGUID>"+
         "<soap:firstRow>1</soap:firstRow>"+
         "<soap:maxRows>200</soap:maxRows>"+
      "</soap:GetActivityInstances>";
	/**
	 * Substitutions for the GET_ACTIVITIES 
	 */ 
	protected static final String STARTS_AFTER_SUBSTR = "%STARTS_AFTER_HERE%";
	protected static final String STARTS_BEFORE_SUBSTR = "%STARTS_BEFORE_HERE%";
	protected static final String ENDS_AFTER_SUBSTR = "%ENDS_AFTER_HERE%";
	protected static final String ENDS_BEFORE_SUBSRT = "%ENDS_BEFORE_HERE%";
	
	/**
	 * This is the soap body for the (major) query where all the cases that the user can try to claim to
	 * are visible in the answer.
	 */
	
	protected static final String GET_ALL_CASES_BODY = "<ns1:GetAllCases/>";
	
	protected static final String CASE_GUID_SUBSTR = "%CASE_GUID_HERE%";
	
	protected static final String GET_PHASES_BY_CASE_GUID_BODY = 
			"<ns1:GetPhasesByCaseGUID>"+
		    "<ns1:caseGUID>"+CASE_GUID_SUBSTR+"</ns1:caseGUID>"+
		    "</ns1:GetPhasesByCaseGUID>";
	
	/**
	 * The constants used to handle the hour entry (claiming)
	 */
	
	protected static final String HOUR_ENTRY_DESC_SUBSTR = "%HOURENTRY_DESC_HERE%";
	protected static final String HOUR_ENTRY_EVENT_DATE_SUBSTR = "%HOURENTRY_EVENT_DATE_HERE%";
	protected static final String HOUR_ENTRY_PHASE_GUID_SUBSTR = "%HOURENTRY_PHASE_GUID_HERE%";
	protected static final String HOUR_ENTRY_QUANTITY_SUBSTR = "%HOUR_ENTRY_QUANTITY_HERE%";
	protected static final String HOUR_ENTRY_USER_GUID_SUBSTR = "%HOUR_ENTRY_USER_GUID_HERE%";
	protected static final String HOUR_ENTRY_WORKTYPE_GUID_SUBSTR = "%HOUR_ENTRY_WORKTYPE_GUID_HERE%";

	protected static final String HOUR_ENTRY_BODY = "<ns1:AddNewHourEntry>"+
         "<ns1:hourEntryInfo>"+
            "<sev:Description>"+HOUR_ENTRY_DESC_SUBSTR+"</sev:Description>"+
            "<sev:EventDate>"+HOUR_ENTRY_EVENT_DATE_SUBSTR+"</sev:EventDate>"+
            "<sev:PhaseGUID>"+HOUR_ENTRY_PHASE_GUID_SUBSTR+"</sev:PhaseGUID>"+
            "<sev:Quantity>"+HOUR_ENTRY_QUANTITY_SUBSTR+"</sev:Quantity>"+
			"<sev:UserGUID>"+HOUR_ENTRY_USER_GUID_SUBSTR+"</sev:UserGUID>"+
			"<sev:WorkTypeGUID>"+HOUR_ENTRY_WORKTYPE_GUID_SUBSTR+"</sev:WorkTypeGUID>"+
			"</ns1:hourEntryInfo>"+
			"</ns1:AddNewHourEntry>";

	/**
	 * Constants for getting handling the getting of work type by guid
	 */
	
	protected static final String WORKTYPES_PHASE_GUID_HERE = "%PHASE_GUID_HERE%";
	
	protected static final String GET_WORKTYPES_BY_PHASE_BODY = 
			"<ns1:GetWorkTypesByPhaseGUID>"+
			"<ns1:phaseGUID>"+WORKTYPES_PHASE_GUID_HERE+"</ns1:phaseGUID>"+
			"</ns1:GetWorkTypesByPhaseGUID>";
	
	/**
	 * Constants for getting the user information
	 */
	
	protected static final String USER_FIRST_NAME_HERE = "%FIRST_NAME_HERE%";
	protected static final String USER_LAST_NAME_HERE = "%LAST_NAME_HERE%";
	
	protected static final String GET_USER_BY_NAME_BODY = "<ns1:GetUserByName>"+
         "<ns1:firstName>"+USER_FIRST_NAME_HERE+"</ns1:firstName>"+
         "<ns1:lastName>"+USER_LAST_NAME_HERE+"</ns1:lastName>"+
         "</ns1:GetUserByName>";	
}
