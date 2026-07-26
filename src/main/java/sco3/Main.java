package sco3;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Main {
	private static final String NATIVE_METHOD_SO = "native-method.so";

	public static native void passString(long address, long length);

	public static native double parseDouble( //
			long address, long length, long error_address //
	);

	public static void main(String[] argv) {
		Main main = new Main();
		main.run();
	}

	void testPass(Arena arena, String sValue) {
		MemorySegment nativeData = arena.allocateFrom(sValue);
		long len = nativeData.byteSize();
		passString(nativeData.address(), len);

	}

	void testDouble(Arena arena, String sValue) {
		MemorySegment nativeData = arena.allocateFrom(sValue);
		long len = nativeData.byteSize();

		MemorySegment errorSeg = arena.allocate(JAVA_LONG);
		double dub = parseDouble(nativeData.address(), len, errorSeg.address());
		long errorCode = errorSeg.get(JAVA_LONG, 0);
		System.out.println("Double: " + dub + " error offset: " + errorCode);
	}

	void run() {
		try (Arena arena = Arena.ofConfined()) {
			testPass(arena, "3.1415926");
			testDouble(arena, "3.1415926");
			testDouble(arena, "3.14asdf6");
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