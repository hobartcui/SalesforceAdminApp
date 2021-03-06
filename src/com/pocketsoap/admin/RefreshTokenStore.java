// Copyright (c) 2011 Simon Fell
//
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included 
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
// THE SOFTWARE.
//

package com.pocketsoap.admin;

import android.content.*;
import android.content.SharedPreferences.Editor;

/** helper for reading/writing the refresh token from the preferences */
public class RefreshTokenStore {

	RefreshTokenStore(Context ctx) {
		this.pref = ctx.getSharedPreferences("a", Context.MODE_PRIVATE);
		this.enc = new EncryptionHelper(DeviceId.getDeviceId(ctx));
	}
	
	private final SharedPreferences pref;
	private final EncryptionHelper enc;
	
	void saveToken(String refreshToken, String authServer) {
		Editor e = pref.edit();
		e.putString(REF_TOKEN, enc.encrypt(refreshToken));
		e.putString(AUTH_SERVER, enc.encrypt(authServer));
		e.commit();
	}
	
	void clearSavedData() {
		Editor e = pref.edit();
		e.clear();
		e.commit();
	}
	
	boolean hasSavedToken() {
		return pref.contains(REF_TOKEN);
	}
	
	String getRefreshToken() {
		String t = pref.getString(REF_TOKEN, null);
		return t == null ? t : enc.decrypt(t);
	}
	
	String getAuthServer() {
		String s = pref.getString(AUTH_SERVER, null);
		return s == null ? Login.PROD_AUTH_HOST : enc.decrypt(s);
	}
	
	
    private static final String REF_TOKEN = "refresh_token";
	private static final String AUTH_SERVER = "authentication_server";
}
