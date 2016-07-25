package com.solera.defrag.exception;

import com.solera.defrag.ViewStack;

/**
 * Exception thrown when {@link ViewStack} is empty and it's impossible to access it's content.
 */
public class EmptyViewStackException extends Exception {

	/**
	 * Constructs a new {@code EmptyViewStackException} without text.
	 */
	public EmptyViewStackException() {
	}
}
