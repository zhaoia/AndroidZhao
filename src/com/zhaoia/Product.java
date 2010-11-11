package com.zhaoia;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;

public class Product {

    public String title = "";
    public String image = "";
    public String price = "";
    public String comment = "";
    public String shop = "";
    public String url = "";
    public Bitmap bp = null;
    public ArrayList<HashMap<String,String>> pas = new ArrayList<HashMap<String,String>>();
    public int num = -1;
    private final String reviewFormat = "评论总数(%d)";
    private final String priceFormat = "价格:%.2f";
    public LinearLayout productView = null;
    
    public Product(JSONObject jobj) {
        init(jobj);
    }

    public Product(JSONObject jobj, int index) {
        num = index;
        init(jobj);
    }
    
    private void init(JSONObject jobj){
    	title = loadStringFromJSON(jobj, "title");
        image = loadStringFromJSON(jobj, "image");
        price = validPrice(loadDoubleFromJSON(jobj, "price"));
        comment = validReview(loadIntFromJSON(jobj, "review"));
        url = loadStringFromJSON(jobj, "url");
        shop = getShopNameFromURL(url);
    }
    
    private String validReview(int d){
    	if (d <= 0){
    		return "暂无价格";
    	} else {
    		return String.format(reviewFormat,d);
    	}
    }
    
    private String validPrice(Double d){
    	if (d <= 0.0){
    		return "暂无评论";
    	} else {
    		return String.format(priceFormat,d);
    	}
    }
    
    public void makeTitleURL(TextView v){
    	v.setText(Html.fromHtml(String.format("<a href=\"%s\">&nbsp;%s</a>",url,title)));
    	v.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    private String getShopNameFromURL(String url){
    	String shopName = "";
    	if (url.contains("360buy"))
    		shopName = "京东商城";
    	else if (url.contains("amazon"))
    		shopName = "卓越亚马逊";
    	else if (url.contains("3c.taobao"))
    		shopName = "淘宝电器城";
    	else if (url.contains("icson"))
    		shopName = "易讯商城";
    	else if (url.contains("newegg"))
    		shopName = "新蛋商城";
    	return shopName;
    }

    private String loadStringFromJSON(JSONObject obj, String s) {
        try {
            return obj.getString(s);
        } catch ( Exception e ) {
        	Log.e("product","load json error:"+e.toString());
            return "";
        }
    }
    
    private Double loadDoubleFromJSON(JSONObject obj, String s) {
        try {
            return obj.getDouble(s);
        } catch ( Exception e ) {
        	Log.e("product","load json error:"+e.toString());
            return 0.0;
        }
    }
    
    private int loadIntFromJSON(JSONObject obj, String s) {
        try {
            return obj.getInt(s);
        } catch ( Exception e ) {
        	Log.e("product","load json error:"+e.toString());
            return 0;
        }
    }
    
    public void getURLImage() {
    	if ( url != "" && bp == null ){
    		InputStream is = null;
    		Log.i("image",image);
    		try {
    			is = (InputStream) new URL(image).getContent();
    			bp = BitmapFactory.decodeStream(is);
    		} catch (Exception e) {
    			Log.e("image", e.toString());
    		} finally {
    			if ( null != is ) {
    				try {
    					is.close();
    				} catch ( IOException e ){}
    			}
    		}
    	}
    }
}
