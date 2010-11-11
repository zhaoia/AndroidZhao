package com.zhaoia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class Util {

    // 下载一个URL的数据
	static public String requestURL( String url ) {
		String ret = "";
		try {
			URL Url = new URL(url);
			URLConnection conn = Url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String rd_read ;
			while ( true ) {
				rd_read = rd.readLine();
				if ( rd_read == null )
					break;
				else
					ret = ret + rd_read;
			}
		} catch ( Exception e) {
		}
		return ret;
	}

    // 缩放图片
    static public Bitmap scaleImage( Bitmap image, int maxWidth, int maxHeight ) {
        Bitmap resizedImage;
        int imageHeight = image.getHeight();
        if ( imageHeight > maxHeight )
            imageHeight = maxHeight;
        int imageWidth = (imageHeight*image.getWidth()) / image.getHeight();
        if ( imageWidth > maxWidth ) {
            imageWidth = maxWidth;
            imageHeight = (imageWidth*image.getHeight()) / image.getWidth();
        }
        resizedImage = Bitmap.createScaledBitmap( image, imageWidth, imageHeight, true);
        return resizedImage;
    }

    // 用浏览器打开一个URL
    static public void GoToWebSite( Context activity, String url ) {
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url) );
        activity.startActivity( intent );
    }
    
    static public SpannableString underline(String s){
    	SpannableString content = new SpannableString(s);
    	content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
    	return content;
    }
    
    static public boolean window_orientaion(Context context){
    	return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    static public View get_view_from_res(Activity context, int resId){
    	LayoutInflater inflater = context.getLayoutInflater();
		return inflater.inflate(resId,null);
    }
    
    // 计算字符串的md5,结果为十六进制大写字符
    static private String getMd5Str(String str) {  
    	MessageDigest md5 = null;
    	try{
    		md5 = MessageDigest.getInstance("MD5");
    		md5.reset();
    		md5.update(str.getBytes());
    	} catch ( Exception e){
    		Log.i("md5",e.toString());
    	}
    	byte[] byteArray = md5.digest();
    	StringBuffer md5StrBuff = new StringBuffer();
    	for (int i=0; i < byteArray.length; i++){
    		if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
    			md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
    		} else {
    			md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
    		}
    	}
    	return md5StrBuff.toString().toUpperCase();
    }

    // 组合请求地址
    // 需要搜索关键字，页数，每页个数
    // appkey,secretcode 视为常量
    static public String getURLWithAppKey(String keyword, int page, int per_page){
        // appkey,keyword,page,per_page,secretcode 顺序不能换
    	String source = String.format("appkey=%s&keyword=%s&page=%d&per_page=%d", Const.AppKey,keyword,page,per_page);
    	String sign = Util.getMd5Str(String.format("%s&secretcode=%s", source,Const.SecretCode));
    	Log.i("sign",sign);
        // 最后的URL里不能包含secretcode这一项
    	return String.format("%s?%s&sign=%s",Const.SearchURL,source,sign);
    }

    static public void toast(Context context, CharSequence content, int gravity, int hm, int vm, int duration){
        Toast t = Toast.makeText(context, content, duration);
        t.setGravity(gravity,0,0);
        t.setMargin((float)hm, (float)vm);
        t.show();
    }
}
