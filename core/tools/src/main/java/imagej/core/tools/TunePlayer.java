/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.tools;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Any QBasic fans out there? ;-)
 * 
 * @author Curtis Rueden
 */
public class TunePlayer {

	private final int sampleRate;

	private byte[] buf = new byte[0];

	private int noteLength = 1;
	private int tempo = 60;
	private int octave = 4;

	public TunePlayer() {
		this(16 * 1000); // 16KHz
	}

	public TunePlayer(final int sampleRate) {
		this.sampleRate = sampleRate;
	}

	// -- TunePlayer methods --

	public int getSampleRate() {
		return sampleRate;
	}

	public int getNoteLength() {
		return noteLength;
	}

	public int getTempo() {
		return tempo;
	}

	public int getOctave() {
		return octave;
	}

	/** Gets the value of the given tone for the current octave. */
	public int getTone(final int step, final char mod) {
		int tone = 12 * (getOctave() - 4) + step;
		if (mod == '#' || mod == '+') tone++;
		if (mod == '-') tone--;
		return tone;
	}

	/** Gets the current note length in milliseconds, by the current tempo. */
	public int getMillis() {
		return toMillis(getNoteLength());
	}

	/** Converts the given note length to milliseconds, by the current tempo. */
	public int toMillis(final int noteLen) {
		// one "beat" is one quarter note; hence:
		// noteLen of 1 = 4 beats per note
		// noteLen of 4 = 1 beat per note
		// noteLen of 8 = 1/2 beat per note
		// generally: beatsPerNote = 4 / noteLen

		// tempo of 60 = 1 second per beat = 1000 ms per beat
		// tempo of 120 = 1/2 second per beat = 500 ms per beat
		// generally: msPerBeat = 6000 / tempo

		// msPerNote = beatsPerNote * msPerBeat = 4 / noteLen * 6000 / tempo

		// TODO - Determine why timing is off by a factor of 10.
		return 10 * 24000 / (noteLen * getTempo());
	}

	public void setNoteLength(final int noteLength) {
		this.noteLength = noteLength;
	}

	public void setTempo(final int tempo) {
		this.tempo = tempo;
	}

	public void setOctave(final int octave) {
		this.octave = octave;
	}

	public void downOctave() {
		octave--;
	}

	public void upOctave() {
		octave++;
	}

	public SourceDataLine openLine() throws LineUnavailableException {
		final AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, true);
		final SourceDataLine line = AudioSystem.getSourceDataLine(af);
		line.open(af, sampleRate);
		line.start();
		return line;
	}

	public void closeLine(final SourceDataLine line) {
		line.drain();
		line.close();
	}

	public boolean play(final String commandString) {
		final SourceDataLine line;
		try {
			line = openLine();
		}
		catch (final LineUnavailableException e) {
			return false;
		}

		final String[] tokens = commandString.toUpperCase().split(" ");
		for (final String token : tokens) {
			final char command = token.charAt(0);
			final String arg = token.substring(1);
			final char mod = token.length() > 1 ? token.charAt(1) : '\0';
			switch (command) {
				case '<': // down one octave
					downOctave();
					break;
				case '>': // up one octave
					upOctave();
					break;
				case 'A':
					play(line, getTone(9, mod));
					break;
				case 'B':
					play(line, getTone(11, mod));
					break;
				case 'C':
					play(line, getTone(0, mod));
					break;
				case 'D':
					play(line, getTone(2, mod));
					break;
				case 'E':
					play(line, getTone(4, mod));
					break;
				case 'F':
					play(line, getTone(5, mod));
					break;
				case 'G':
					play(line, getTone(7, mod));
					break;
				case 'L': // change note length
					setNoteLength(Integer.parseInt(arg));
					break;
				case 'M': // change music mode
					// TODO
					break;
				case 'N': // note
					final int note = Integer.parseInt(arg);
					if (note == 0) play(line, null);
					else play(line, note - 48);
					break;
				case 'O': // change octave
					setOctave(Integer.parseInt(arg));
					break;
				case 'P': // pause
					int len;
					try {
						len = Integer.parseInt(arg);
					}
					catch (final NumberFormatException exc) {
						len = noteLength;
					}
					play(line, null, toMillis(len));
					break;
				case 'T': // change tempo
					setTempo(Integer.parseInt(arg));
					break;
				default:
					throw new RuntimeException("Unknown command: " + command);
			}
		}

		closeLine(line);
		return true;
	}

	// -- Helper methods --

	private void play(final SourceDataLine line, final Integer tone) {
		play(line, tone, getMillis());
	}

	private void
		play(final SourceDataLine line, final Integer tone, final int ms)
	{
		final int length = fill(tone, ms);
		int count = 0;
		while (count < length) {
			final int r = line.write(buf, count, length - count);
			if (r <= 0) throw new RuntimeException("Could not write to line");
			count += r;
		}
	}

	/**
	 * @param tone Use 1 for A4, +1 for half-step up, -1 for half-step down.
	 * @param ms Milliseconds of data to fill.
	 * @return Length of buffer filled, in bytes.
	 */
	private int fill(final Integer tone, final int ms) {
		final int length = sampleRate * ms / 1000;
		if (length > buf.length) {
			// ensure internal buffer is large enough
			buf = new byte[length];
		}
		if (tone == null) {
			// rest data
			Arrays.fill(buf, 0, length, (byte) 0);
		}
		else {
			// tone data
			final double exp = ((double) tone - 1) / 12d;
			final double f = 440d * Math.pow(2d, exp);
			for (int i = 0; i < length; i++) {
				final double period = sampleRate / f;
				final double angle = 2 * Math.PI * i / period;
				buf[i] = (byte) (127 * Math.sin(angle));
			}
		}
		return length;
	}

}
