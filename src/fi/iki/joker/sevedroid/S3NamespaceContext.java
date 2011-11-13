package fi.iki.joker.sevedroid;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import android.util.Log;

public class S3NamespaceContext implements NamespaceContext {

	private static final String TAG = "Sevedroid";
	
	@Override
	public String getNamespaceURI(String prefix) {
		Log.d(TAG, "Asked namespaceURI for prefix: "+prefix+".");
		if("s".equals(prefix)) {
			return "http://schemas.xmlsoap.org/soap/envelope/";
		} 
		if ("a".equals(prefix)) {
			return "http://schemas.datacontract.org/2004/07/Severa.Entities.API";
		}
		if ("x".equals(prefix)) {
			return "http://soap.severa.com/";
		}
		return null;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		Log.d(TAG, "Asked prefix for namespaceURI:"+namespaceURI);
		if("http://schemas.xmlsoap.org/soap/envelope/".equals(namespaceURI)) {
			return "s";
		} 
		if ("http://schemas.datacontract.org/2004/07/Severa.Entities.API".equals(namespaceURI)) {
			return "a";
		} 
		if("http://soap.severa.com/".equals(namespaceURI)) {
			return "x";
		}
		return null;
	}

	@Override
	public Iterator getPrefixes(String namespaceURI) {
		Log.d(TAG, "Asked Iterator of prefixes for namespaceURI:"+namespaceURI);
		return null;
	}
}
