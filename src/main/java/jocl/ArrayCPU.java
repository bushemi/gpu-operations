package jocl;

public class ArrayCPU {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        int n = 10_000_000;
        float srcArrayA[] = new float[n];
        float srcArrayB[] = new float[n];
        float dstArray[] = new float[n];
        for (int i = 0; i <n; i++){
            srcArrayA[i]= i;
            srcArrayB[i]= i;
        }

        long startComputation = System.currentTimeMillis();

        for (int i = 0; i <n; i++){
            dstArray[i] = srcArrayA[i] + srcArrayB[i];
        }

        System.out.println("Computation took in: "+ (System.currentTimeMillis() - startComputation) + " ms.");
        System.out.println("Whole process took in: "+ (System.currentTimeMillis() - start) + " ms.");

    }
}
