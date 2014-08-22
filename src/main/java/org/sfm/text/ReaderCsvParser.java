package org.sfm.text;

import java.io.IOException;
import java.io.Reader;

public final class ReaderCsvParser {
	
	static enum State {
		IN_QUOTE, QUOTE, NONE
	}
	private char[] buffer;

	private int bufferLength;
	private State currentState = State.NONE;

	private int currentStart =0;
	private int bufferOffset = 0;

	
	public ReaderCsvParser(final int bufferSize) {
		buffer = new char[bufferSize];
	}
	
	/**
	 * parse cvs from input stream assumes character encoding for '"', ',' and '\n' match utf8
	 * @param is
	 * @param handler
	 * @return
	 * @throws IOException
	 */
	public void parse(final Reader is, final CharsCellHandler handler) throws IOException {
		char c = 0;
		
		while((bufferLength = is.read(buffer, bufferOffset, buffer.length - bufferOffset)) != -1) {
			c = consumeBytes(handler);
		}
		
		if (bufferOffset > 0 || c == ',' ) {
			handler.newCell(buffer, 0, bufferOffset);
		}
	}


	private char consumeBytes(final CharsCellHandler handler) {
		bufferLength += bufferOffset;
		
		char c = 0;
		for(int i = 0; i < bufferLength; i++) {
			c = buffer[i];
			handleByte(handler, c, i);
		}
		
		shiftBuffer();
		
		return c;
	}

	private void handleByte(final CharsCellHandler handler, final char c, final int i) {
		if (c == '"') {
			if (currentStart == i) {
				currentState = State.IN_QUOTE;
			} else if (currentState == State.IN_QUOTE) {
				currentState = State.QUOTE;
			} else {
				if (currentState == State.QUOTE) {
					currentState = State.IN_QUOTE;
				}
			}
		} else if (c == ',' ) {
			if (currentState != State.IN_QUOTE) {
				handler.newCell(buffer, currentStart, i - currentStart);
				currentStart = i  + 1;
				currentState = State.NONE;
			}
		}else if (c == '\n') {
			if (currentState != State.IN_QUOTE) {
				handler.newCell(buffer, currentStart, i - currentStart);
				currentStart = i  + 1;
				currentState = State.NONE;
				handler.newRow();
			}
		}
	}

	private void shiftBuffer() {
		// shift buffer consumer data
		bufferOffset = bufferLength - currentStart;
		
		// if buffer tight double the size
		if (bufferOffset > bufferLength >> 1) {
			// double buffer size
			char[] newbuffer = new char[buffer.length << 1];
			System.arraycopy(buffer, currentStart, newbuffer, 0, bufferOffset);
			buffer = newbuffer;
		} else {
			System.arraycopy(buffer, currentStart, buffer, 0, bufferOffset);
		}
		currentStart = 0;
	}
}