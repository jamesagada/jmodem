package jmodem;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sender {

	private final float[] carrier;

	private short[] symbol;
	private byte[] word;

	private OutputStream out;

	public Sender(OutputStream o) {
		 carrier = new float[Config.Nsym];
		 for (int i = 0; i < carrier.length; i++) {
			 carrier[i] = (float)Math.sin((2 * Math.PI * Config.Fc * i) / Config.Fs);
		 }
		 symbol = new short[carrier.length];
		 word = new byte[2];
		 out = o;
	}

	void send(float amplitude, int n) throws IOException {
		for (int i = 0; i < symbol.length; i++) {
			symbol[i] = (short)(Config.SCALING * amplitude * carrier[i]);
		}
		for (int i = 0; i < n; i++) {
			for (short value : symbol) {
				word[0] = (byte)(value & 0xFF);
				word[1] = (byte)(value >> 8);
				out.write(word);
			}
		}
	}


	public static void main(String []args) throws IOException {
		OutputStream out = System.out;
		InputStream in = System.in;
		Sender s = new Sender(new BufferedOutputStream(out, 1024));
		
		s.send(0f, 500);
		s.send(1f, 400);
		s.send(0f, 100);

		int r = 0x1;
		for (int i = 0; i < 200; ++i) {
			r = Config.prbs(r, 16, 0x1100b);
			s.send(2f * (r & 1) - 1, 1);
		}
		s.send(0f, 100);
		
		while (true) {
			int b = in.read();
			if (b == -1) {
				break;
			}
			for (int i = 0; i < 8; i++) {
				int bit = (b >> i) & 1;
				s.send(2f * bit - 1, 1);
			}
			
		}
		s.send(0f, 500);
		out.flush();
		out.close();
	}

}
