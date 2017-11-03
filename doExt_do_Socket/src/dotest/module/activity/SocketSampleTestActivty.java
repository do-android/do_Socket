package dotest.module.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import core.DoServiceContainer;
import doext.implement.do_Socket_Model;
import dotest.module.frame.debug.DoService;
/**
 * Socket组件测试
 */
public class SocketSampleTestActivty extends DoTestActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void initModuleModel() throws Exception {
		this.model = new do_Socket_Model();
	}
	
	@Override
	protected void initUIView() throws Exception {
	}
	
	public void connect(View view){
		Toast.makeText(this, "connect", Toast.LENGTH_SHORT).show();
		//连接
		Map<String, String>  _paras_connectString = new HashMap<String, String>();
		_paras_connectString.put("ip", "192.168.1.114");
		_paras_connectString.put("port", "3000");
        DoService.asyncMethod(SocketSampleTestActivty.this.model, "connect", _paras_connectString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {//回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}
	
	public void send(View view){
		Toast.makeText(this, "send", Toast.LENGTH_SHORT).show();
		//发送数据
		Map<String, String>  _paras_sendString = new HashMap<String, String>();
		_paras_sendString.put("content", "123456    你好     abcd ~！@#￥%");
        DoService.asyncMethod(SocketSampleTestActivty.this.model, "send", _paras_sendString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {//回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}
	public void close(View view){
		Toast.makeText(this, "close", Toast.LENGTH_SHORT).show();
		// 关闭
		Map<String, String> _paras = new HashMap<String, String>();
		DoService.syncMethod(SocketSampleTestActivty.this.model, "close", _paras);
	}

	@Override
	public void doTestProperties(View view) {
		 DoService.setPropertyValue(this.model, "url", "https://www.baidu.com");
	}

	@Override
	protected void doTestSyncMethod() {
	
	}

	@Override
	protected void doTestAsyncMethod() {
		
	}

	@Override
	protected void onEvent() {
		DoService.subscribeEvent(this.model, "receive", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("事件回调：" + _data);
			}
		});
		DoService.subscribeEvent(this.model, "error", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("事件回调：" + _data);
			}
		});
	}

	@Override
	public void doTestFireEvent(View view) {
		DoService.subscribeEvent(this.model, "receive", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("事件回调：" + _data);
			}
		});
		
	}

}
