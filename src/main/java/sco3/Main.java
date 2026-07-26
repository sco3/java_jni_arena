package sco3;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Main {
	private static final String NATIVE_METHOD_SO = "native-method.so";

	public static native void passString(long address, long length);

	public static void main(String[] argv) {
		Main main = new Main();
		main.run();
	}

	void run() {
		try (Arena arena = Arena.ofConfined()) {
			String snum = "3.1415926";

			MemorySegment nativeData = arena.allocateFrom(snum);
			long len = nativeData.byteSize();
			passString(nativeData.address(), len);

		}

	}

	static {
		File soFile = new File(NATIVE_METHOD_SO);
		if (soFile.exists()) {
			try {
				System.load(soFile.getAbsolutePath());

			} catch (UnsatisfiedLinkError e) {
			}

		} else {
			System.out.println("Library not found: " + NATIVE_METHOD_SO);
		}
	}
}