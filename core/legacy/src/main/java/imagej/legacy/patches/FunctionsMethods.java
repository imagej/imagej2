// TODO clean up

package imagej.legacy.patches;

public class FunctionsMethods {
	
	public static int InsideBatchDrawing = 0;
	
	public static void beforeBatchDraw() {
		InsideBatchDrawing++;
	}

	public static void afterBatchDraw() {
		InsideBatchDrawing--;
	}
}
