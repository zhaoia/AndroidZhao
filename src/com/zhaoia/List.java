package com.zhaoia;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.zhaoia.List.ProductContainer.Loading;

public class List extends Activity{
    
    private EventHandler msgHandler = new EventHandler(Looper.myLooper());
    private ProductContainer productsContainer = new ProductContainer();   
    private ProductAdapter productAdapter = new ProductAdapter();
    private String keyword;
    private ListView productViewsContainer;
    private EditText edittext;
    private Dialog pop = null;
    private ProgressDialog myDialog = null;
    private Loading loading = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
    	Log.i("oncreate","list oncreate begin");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        keyword = getIntent().getStringExtra("k");
        Log.i("list","get keyword:"+keyword);
        
        edittext = (EditText)findViewById(R.id.input);
        edittext.setText(keyword);
        productViewsContainer = (ListView)findViewById(R.id.container);
        productViewsContainer.setAdapter( productAdapter );
        productViewsContainer.setOnScrollListener(new OnScrollListener());
        productsContainer.checkTotalRows();
        msgHandler.sendMessage(msgHandler.obtainMessage(0,0,5));
    }
    
    // 屏幕滚动到最下面则显示下一个
    class OnScrollListener implements AbsListView.OnScrollListener {
    	private boolean lastPosition = false;
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			Log.i("on scroll", String.format("firstVisibleItem:%d, visibleItemCount:%d, totalItems:%d",firstVisibleItem,visibleItemCount,totalItemCount));
			if ( firstVisibleItem + visibleItemCount == totalItemCount ){
				lastPosition = true;
			} else 
				lastPosition = false;
		}
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			Log.i("scroll state", "scrollState:"+scrollState);
			if ( scrollState == 0 && lastPosition == true){
                Log.i("scroll state","next, state:"+scrollState);
                Util.toast(List.this, "正在显示下一个",Gravity.CENTER, 0, Const.HEIGHT-5, 300);
				productAdapter.next();
			}
		}
    }

    class ProductAdapterView{
        // 新开一个线程下载图片，下载完成后发送一个消息
        // 系统在接受到这个消息后显示图片
    	private class LoadImage extends Thread{
    		private Product p;
    		LoadImage(Product product){
    			p = product;
    		}
    		public void run(){
    			p.getURLImage();
    			Message msg = msgHandler.obtainMessage(2, p);
    			msgHandler.sendMessage(msg);
    		}
    	}
    	public LinearLayout shine(Product p, int resId){
    		LinearLayout productView = (LinearLayout) Util.get_view_from_res(List.this,resId);
    		p.productView = productView;
    		shineTitle(productView, p);
    		((TextView)(productView.findViewById(R.id.sprice))).setText(p.price);
    		((TextView)(productView.findViewById(R.id.scomment))).setText(p.comment);
    		((TextView)(productView.findViewById(R.id.sshop))).setText(p.shop);
    		if (p.bp == null)
    			(new LoadImage(p)).start();
    		else
    			shineProductImage(p);
    		productView.setVisibility(View.VISIBLE);
    		return productView;
    	}
    	private void shineTitle(LinearLayout productView, Product p){
    		p.makeTitleURL((TextView)(productView.findViewById(R.id.stitle)));
    	}
    }

    class ProductAdapter extends BaseAdapter {  
        private ArrayList<Product> products;
        ProductAdapter(){
        	products = new ArrayList<Product>();
        }
        public void next(){
        	Product p = productsContainer.next();
            if ( p!= null ){
                products.remove(0);
                products.add(p);
                notifyDataSetChanged();
                if (p.productView != null) {
                    //p.productView.startAnimation( Anim.trans(0-Const.WIDTH, 800, 2.0, 1000) );
                }
            }
        }
        public void reset( int start, int end ){
        	ArrayList<Product> productList = null;
        	try{
        		productList = productsContainer.get(start,end);
        	} catch (IndexOutOfBoundsException e) {
        		productList = null;
        	}
            if ( productList != null ){
            	Log.i("ready to show products","productList size:"+productList.size());
                products = productList;
                notifyDataSetChanged();
                hideWaitingDialog();
            }
        }
        public int getCount() {                          
            return products.size();  
        }  
        public Product getItem(int position) {       
            return products.get(position);  
        }  
        public long getItemId(int position) {    
            return position;  
        }  
        public View getView(int position, View convertView, ViewGroup parent) {   
            Product p = getItem(position);  
            return new ProductAdapterView().shine(p, R.layout.product);  
        }  
    }  

    private void showWaitingDialog(){
        if (myDialog == null || !myDialog.isShowing()){
            Log.i("dialog","show");
            myDialog = ProgressDialog.show( List.this, getResources().getString(R.string.dialog_title), getResources().getString(R.string.dialog_content), true );
        }
    }
    private void hideWaitingDialog(){
        Log.i("dialog","hide");
        try{
            myDialog.dismiss();
        } catch ( Exception e ){
            if (myDialog != null && myDialog.isShowing()){
                myDialog.dismiss();
            }
        }
    }

    class ProductContainer {

        private ArrayList<Product> list;
        private int top,down;
        private final int step = 10;
        private int total_rows;
        private int page = 1;

        ProductContainer(){
        	list = new ArrayList<Product>();
			top=-1;down=-1;
        }
        
        // 第一次获取数据得到结果总数
        // 如果为零则回到首页， 否则开始请求搜索结果数据
        private void checkTotalRows(){
            showWaitingDialog();
        	total_rows = getTotalRows();
        	if ( total_rows == 0 ){
                hideWaitingDialog();
                Util.toast(List.this,String.format("(-.-) 没有发现与 %s 相关的商品",keyword),Gravity.CENTER, 0, 0, 5000);
				Intent intent = new Intent();
                intent.setClass(List.this, Zhaoia.class);
                startActivity( intent );
			}
        }
        
        private int getTotalRows(){
        	String url = Util.getURLWithAppKey(keyword,1,1);
            String response = Util.requestURL(url);
            int total_rows = 0;
			try {
				total_rows = (new JSONObject(response)).getInt("total_rows");
			} catch (JSONException e) {
				Log.e("total_rows", e.toString());
			}
			Log.i("total_rows",Integer.toString(total_rows));
			return total_rows;
        }

        private void add(Product p){
            if ( p!= null )
                list.add(p);
        }

        private Product get(int index){
            if ( index<list.size() && index>=0 ){
                return list.get(index);
            } else{
                return null;
            }
        }

        private void preloading(){
            if ( down+1 < total_rows && down + step > list.size() && (loading == null || !loading.isAlive()) ){
                loading = new Loading();
                loading.start();
            }
        }

        public Product previous(){
            if ( top <= 0 )
                return null;
            else{
                top--;down--;
                return get(top);
            }
        }

        public Product next(){
            if ( down+1 < total_rows && down+1 >= list.size() ){
                if ( loading != null && !loading.isAlive() ){
                    loading = new Loading(msgHandler.obtainMessage(1));
                    loading.start();
                }
                return null;
            } else{
                down++;top++;
                preloading();
                return get(down);
            }
        }

        public ArrayList<Product> get(int start, int end){
        	Log.i("get products",String.format("start:%d, end:%d",start,end));
        	Log.i("product container size",list.size()+"");
            if ( start >= 0 && start < end ){
                if ( down+1 < total_rows && end+1 > list.size() && (loading == null || !loading.isAlive()) ){
                    loading = new Loading(msgHandler.obtainMessage(0,start,end));
                    loading.start();
                    return null;
                }else{
                    ArrayList<Product> ret = new ArrayList<Product>();
                    for (; start <= end ; start++)
                        ret.add(list.get(start));
                    top = start; down = end;
                    preloading();
                    return ret;
                }
            } else
                return null;
        }

        class Loading extends Thread{
            private Message msg = null;
            private int number = step;
            Loading(){}
            Loading(Message msg){ 
                this.msg = msg; 
                if ( msg.what == 0 ){
                    int arg2 = msg.arg2;
                    if ( arg2 > down + number )
                        number = arg2 - down;
                }
            }
            // 对得到的json数据进行解析
            // response 是返回的字符串，是一个字典，哈希，或者叫映射的json
            // {"total_rows":896,"product_lists":[{...},{...},...]}
            private void parseResponse(String response){
                JSONArray array = null;
				try {
					JSONObject dict = new JSONObject(response);
					//Log.i("dict",dict.toString());
					array = dict.getJSONArray("product_lists");
					//Log.i("array",array.toString());
				} catch (JSONException e) {
					Log.e("parse response",e.toString());
				}
                // product_lists 是一个字典的数组
				JSONObject dict = null;
                for ( int index=0; index < array.length(); index++ ) {
                	try {
                		dict = array.getJSONObject(index);
                	} catch ( JSONException e ) 
                	{ 
                		dict = null; 
                	}
                	if ( dict != null )
                		add( new Product(dict) );
                }
            }
            
            public void run(){
            	Log.i("before loading", String.format("list size:%d, page:%d, top:%d, down:%d",list.size(),page,top,down));
                // 向服务器请求数据
                // 首先要计算出url
                // 详细请见 Util.getURLWithAppKey
                String url = Util.getURLWithAppKey(keyword,page,number);
                Log.i("url",url);
                String response = Util.requestURL(url);
                //Log.i("json",response);
                parseResponse(response);
                page++;
                if ( msg != null ){
                    msgHandler.sendMessage(msg);
                	Log.i("send msg", msg.toString());
                }
                Log.i("after loading", String.format("list size:%d, page:%d, top:%d, down:%d",list.size(),page,top,down));
            }
        }
    }
    
    // 点击图片弹出大图显示
    private void shineProductImage(final Product p){
    	LinearLayout productView = p.productView;
		ImageView img = (ImageView)(productView.findViewById(R.id.simage));
		img.setImageBitmap(p.bp);
		//img.startAnimation( Anim.alpha(0.5, 800, 2.0, 200) );
		img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                popProductImage(p);
            }
		});	
	}
    private void popProductImage(final Product product){
        if ( pop!=null && pop.isShowing() )
            pop.cancel();
        if (product.bp != null){
        pop = new Dialog(List.this,R.style.pop_image){
            @Override
            public boolean onTouchEvent(MotionEvent event){
                dismiss();
                return false;
            }   
        };  
        LinearLayout v = (LinearLayout)(Util.get_view_from_res(List.this,R.layout.image_pop));
        if (Util.window_orientaion(List.this)){
            v.setOrientation(LinearLayout.HORIZONTAL);
        }   
        pop.setContentView(v);
        pop.setCancelable(true);
        final int l = Const.WIDTH > Const.HEIGHT ? Const.HEIGHT : Const.WIDTH;
        ImageView popi = (ImageView)pop.findViewById(R.id.popi);
        popi.setImageBitmap(Util.scaleImage(product.bp, l, l));
        TextView popt = (TextView)pop.findViewById(R.id.popt);
        popt.setText(Util.underline(product.title));
        popt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	pop.dismiss();
                Util.GoToWebSite(List.this, product.url);          	
                return false;
            }   
        }); 
        popi.startAnimation(AnimationUtils.loadAnimation(List.this, R.anim.fade_in));
        Animation anim = AnimationUtils.loadAnimation(List.this, R.anim.hide);
        anim.setStartOffset(1000);
        popt.startAnimation(anim);
        pop.show();
        }   
    }

    class EventHandler extends Handler{
        public EventHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                // 显示arg1到arg2
                case 0:
                	Log.i("get msg 0", msg.arg1+" "+msg.arg2);
                    productAdapter.reset(msg.arg1,msg.arg2);
                    break;
                // 显示下一个
                case 1:
                	Log.i("get msg 1", "next");
                    productAdapter.next();
                    break;
                // 显示产品图片
                case 2:
                	//Log.i("image","load image ok");
                	Product p = (Product)msg.obj;
                	shineProductImage(p);
                	break;
                default:
                    break;
            }
        }
    }
}
