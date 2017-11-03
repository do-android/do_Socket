package doext.implement;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import doext.define.do_Socket_IMethod;
import doext.define.do_Socket_MAbstract;
import doext.utils.SocketUtils;

/**
 * 自定义扩展MM组件Model实现，继承do_Socket_MAbstract抽象类，并实现do_Socket_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Socket_Model extends do_Socket_MAbstract implements do_Socket_IMethod {

	private DoIScriptEngine scriptEngine;
	private String callbackFuncName;
	private Socket socket;
	private static final int CONNECT_SUCCESS = 0;
	private static final int CONNECT_FAILURE = -1;
	private static final int RECIVE_MESSAGE = 1;
	private OutputStream outputStream;

	public do_Socket_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("close".equals(_methodName)) {
			this.close(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName
	 *                    ,_invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("connect".equals(_methodName)) {
			this.connect(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("send".equals(_methodName)) {
			this.send(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 关闭链接；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void close(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		try {
			if (socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
				socket = null;
				outputStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CONNECT_SUCCESS:
				try {
					outputStream = socket.getOutputStream();
					registReceiver();
					callBack(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case CONNECT_FAILURE:
				callBack(false);
				try {
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case RECIVE_MESSAGE:
				DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
				String _rs = (String) msg.obj;
				_invokeResult.setResultText(_rs);
				if (getEventCenter() != null) {
					getEventCenter().fireEvent("receive", _invokeResult);
				}
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 连接；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void connect(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		this.scriptEngine = _scriptEngine;
		this.callbackFuncName = _callbackFuncName;
		final String _ip = DoJsonHelper.getString(_dictParas, "ip", "");
		final String _port = DoJsonHelper.getString(_dictParas, "port", "");
		if (TextUtils.isEmpty(_ip) || TextUtils.isEmpty(_port)) {
			callBack(false);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket = new Socket(_ip, Integer.parseInt(_port));
					handler.sendMessage(handler.obtainMessage(CONNECT_SUCCESS));
				} catch (Exception e) {
					handler.sendMessage(handler.obtainMessage(CONNECT_FAILURE));
					DoServiceContainer.getLogEngine().writeError("服务端ip或者端口号找不到", e);
				}
			}
		}).start();
	}

	/**
	 * 发送数据；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void send(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		this.scriptEngine = _scriptEngine;
		this.callbackFuncName = _callbackFuncName;
		if (socket != null) {
			final String _content = DoJsonHelper.getString(_dictParas, "content", "");
			final String _type = DoJsonHelper.getString(_dictParas, "type", "");
			if (TextUtils.isEmpty(_content)) {
				callBack(false);
				return;
			}
			try {
				if (_type.equalsIgnoreCase("HEX")) {//发送十六进制数
					writeHex(_content);
				} else if (_type.equalsIgnoreCase("File")) {//发送文件	
					writeFile(_content);
				} else {
					writeStr(_content);//发送字符串	
				}
				callBack(true);
			} catch (Exception e) {
				callBack(false);
				e.printStackTrace();
				DoServiceContainer.getLogEngine().writeError("发送异常", e);
			}
		} else {
			DoServiceContainer.getLogEngine().writeInfo("发送异常，socket没有建立连接", "do_Socket");
		}
	}

	private void writeStr(String _data) throws Exception {
		DataOutputStream out = new DataOutputStream(outputStream);
		out.write(_data.getBytes());
	}

	private void writeFile(String _data) {
		FileInputStream reader = null;
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(outputStream));
			reader = new FileInputStream(getLocalPath(_data));
			byte buf[] = new byte[2048];
			int len = 0;
			while ((len = reader.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void writeHex(String _data) throws Exception {
		DataOutputStream out = new DataOutputStream(outputStream);
		out.write(SocketUtils.hexStr2Byte(_data));
	}

	public void callBack(boolean result) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		_invokeResult.setResultBoolean(result);
		scriptEngine.callback(callbackFuncName, _invokeResult);
	}

	private void registReceiver() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = socket.getInputStream();
					DataInputStream dis = new DataInputStream(is);
					byte[] buffer = new byte[4 * 1024];
					int len = 0;
					while ((len = dis.read(buffer)) != -1) {
						String _result = SocketUtils.bytesToHexString(buffer, len);
						handler.sendMessage(handler.obtainMessage(RECIVE_MESSAGE, _result));
					}
				} catch (Exception e) {
					DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
					JSONObject jsonNode = new JSONObject();
					try {
						jsonNode.put("message", "socket连接异常，" + e.getMessage());
					} catch (Exception ex) {
					}
					_invokeResult.setResultNode(jsonNode);
					getEventCenter().fireEvent("error", _invokeResult);
					DoServiceContainer.getLogEngine().writeError("socket 连接异常", e);
				}
			}
		}).start();
	}

	private String getLocalPath(String local) throws Exception {
		return DoIOHelper.getLocalFileFullPath(scriptEngine.getCurrentPage().getCurrentApp(), local);
	}

}