package doext.utils;

import java.io.UnsupportedEncodingException;

public class SocketUtils {

	public static String bytesToHexString(byte[] src, int len) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return "";
		}
		for (int i = 0; i < len; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String bytes2hex02(byte bs[]) {
		String ZERO = "00000000";
		String result = null;
		for (int i = 0; i < bs.length; i++) {
			result = Integer.toBinaryString(bs[i]);
			if (result.length() > 8) {
				result = result.substring(result.length() - 8);
			} else if (result.length() < 8) {
				result = ZERO.substring(result.length()) + result;
			}
		}
		return result;
	}

	public static String bytes2str(byte bs[], String d) {
		String result = null;
		try {
			result = new String(bs, d);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] hexStr2Byte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	public static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}
}
