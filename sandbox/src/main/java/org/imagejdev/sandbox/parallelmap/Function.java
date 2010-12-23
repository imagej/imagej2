package org.imagejdev.sandbox.parallelmap;

// from http://ibhana.wordpress.com/2009/08/

// Generic Function - the input is some type I and the output is R

public interface Function<I, R>
{
	public R apply(I item) throws Exception;
}
