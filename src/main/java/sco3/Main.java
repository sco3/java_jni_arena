package sco3;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class Main {
	private static final String BAD_PI = "3.14asdf6";
	private static final String PI = "3.1415926";
	private static final String NATIVE_METHOD_SO = "native-method.so";
	private static final String NATIVE_METHOD_RUST_SO = "libnative_method_rust.so";
	private static final String[] LIBS = { NATIVE_METHOD_SO, NATIVE_METHOD_RUST_SO };

	public static native void passString(long address, long length);

	public static native double parseDouble( //
			long address, long length, long error_address //
	);

	public static native void passStringRust(long address, long length);

	public static native double parseDoubleRust( //
			long address, long length, long error_address//
	);

	public static native double parseFastFloatRust( //
			long address, long length, long error_address//
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

	void testPassRust(Arena arena, String sValue) {
		MemorySegment nativeData = arena.allocateFrom(sValue);
		long len = nativeData.byteSize();
		passStringRust(nativeData.address(), len);

	}

	void testDouble(Arena arena, String sValue, MemorySegment nativeData,
			MemorySegment errorSeg, Map<String, Long> metrics) {
		long ns = System.nanoTime();

		var bytes = sValue.getBytes(StandardCharsets.UTF_8);
		int len = bytes.length;
		MemorySegment.copy(bytes, 0, nativeData, ValueLayout.JAVA_BYTE, 0, len);

		double dub = parseDouble(nativeData.address(), len, errorSeg.address());
		long errorCode = errorSeg.get(JAVA_LONG, 0);
		long took = System.nanoTime() - ns;

		String k = "C double: " + ((errorCode < 0) ? dub : "n/a");
		metrics.merge(k, took, Long::sum);
	}

	void testDoubleRust(Arena arena, String sValue, MemorySegment nativeData,
			MemorySegment errorSeg, Map<String, Long> metrics) {
		long ns = System.nanoTime();
		var bytes = sValue.getBytes(StandardCharsets.UTF_8);
		int len = bytes.length;
		MemorySegment.copy(bytes, 0, nativeData, ValueLayout.JAVA_BYTE, 0, len);

		double dub = parseDoubleRust(nativeData.address(), len, errorSeg.address());
		long errorCode = errorSeg.get(JAVA_LONG, 0);
		long took = System.nanoTime() - ns;
		String k = "Rust double: " + ((errorCode < 0) ? dub : "n/a");
		metrics.merge(k, took, Long::sum);
	}

	void testFastFloatRust(Arena arena, String sValue, MemorySegment nativeData,
			MemorySegment errorSeg, Map<String, Long> metrics) {
		long ns = System.nanoTime();
		var bytes = sValue.getBytes(StandardCharsets.UTF_8);
		int len = bytes.length;
		MemorySegment.copy(bytes, 0, nativeData, ValueLayout.JAVA_BYTE, 0, len);

		double dub = parseFastFloatRust(nativeData.address(), len,
				errorSeg.address());
		long errorCode = errorSeg.get(JAVA_LONG, 0);
		long took = System.nanoTime() - ns;
		String k = "Rust fast float: " + ((errorCode < 0) ? dub : "n/a");
		metrics.merge(k, took, Long::sum);
	}

	void testJava(String sValue, Map<String, Long> metrics) {
		long ns = System.nanoTime();
		long errorCode = -1;
		double dub = 0;
		try {
			dub = Double.parseDouble(sValue);

		} catch (Exception e) {
			errorCode = 1;
		}
		long took = System.nanoTime() - ns;
		String k = "Java double: " + ((errorCode < 0) ? dub : "n/a");
		metrics.merge(k, took, Long::sum);
	}

	void run() {
		Map<String, Long> metrics = new TreeMap<String, Long>();
		int n = 10000000;

		try (Arena arena = Arena.ofConfined()) {
			var err_seg = arena.allocate(JAVA_LONG);
			var data_seg = arena.allocate(1024);
			testPass(arena, PI);
			testPassRust(arena, PI);

			out.println("\nRun " + n + " tests\n");

			for (int i = 0; i < n; i++) {
				testJava(PI, metrics);
				testDouble(arena, PI, data_seg, err_seg, metrics);
				testDoubleRust(arena, PI, data_seg, err_seg, metrics);
				testFastFloatRust(arena, PI, data_seg, err_seg, metrics);

				testJava(BAD_PI, metrics);
				testDouble(arena, BAD_PI, data_seg, err_seg, metrics);
				testDoubleRust(arena, BAD_PI, data_seg, err_seg, metrics);
				testFastFloatRust(arena, BAD_PI, data_seg, err_seg, metrics);

			}
			for (var e : metrics.entrySet()) {
				var label = format("%-32s", e.getKey());
				var value = format("%16.2f ns", 1.0 * e.getValue() / n);
				out.println(label + value);

			}
		}
		System.exit(0);
	}

	static {
		for (String soName : LIBS) {
			File soFile = new File(soName);
			if (soFile.exists()) {
				try {
					System.load(soFile.getAbsolutePath());

				} catch (UnsatisfiedLinkError e) {
				}

			} else {
				System.out.println("Library not found: " + soName);
			}
		}
	}
}