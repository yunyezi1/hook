package com.spread.hook;

public class ReflectException extends RuntimeException {
	private static final long serialVersionUID = -2243843843843438438L;

	public ReflectException() {
	}

	public ReflectException(String err) {
		super(err);
	}

	public ReflectException(String tag, Throwable th) {
		super(tag, th);
	}

	public ReflectException(Throwable th) {
		super(th);
	}
}