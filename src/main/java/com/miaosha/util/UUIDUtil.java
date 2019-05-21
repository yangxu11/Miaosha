package com.miaosha.util;

import java.util.UUID;

public class UUIDUtil {
	public static final int LENGTH = 32;
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
