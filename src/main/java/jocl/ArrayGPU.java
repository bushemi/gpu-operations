package jocl;

import org.jocl.*;

import static org.jocl.CL.*;

public class ArrayGPU {

    /*
    * source code of the OpenCL program to execute
    */
    private static String programSource =
        "__kernel void " +
        "sampleKernel(__global const float *a," +
                    "__global const float *b," +
                    "__global float *c)" +
        "{" +
        "int gid = get_global_id(0);" +
        "c[gid] = a[gid] + b[gid];" +
        "}";


    /** The entry point
     *
     * @param args
     */
    public static void main(String[] args) {

        //        Create input and output data
        long start = System.currentTimeMillis();

        int n = 10_000_000;
        float srcArrayA[] = new float[n];
        float srcArrayB[] = new float[n];
        float dstArray[] = new float[n];
        for (int i = 0; i <n; i++){
            srcArrayA[i]= i;
            srcArrayB[i]= i;
        }
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);

        // the platform , device type and device number
        //that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;
        // enable exceptions and subsequently omit errors checks in this sample
        CL.setExceptionsEnabled(true);

        //obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        //obtain Platform Id
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        //initialize the context properties
        cl_context_properties context_properties = new cl_context_properties();
        context_properties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain the device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // print platform info
        String deviceName = getString(device, CL_DEVICE_NAME);
        System.out.println("Device: "+ deviceName);

        //create a context for the selected device
        cl_context context = clCreateContext(
                context_properties, 1, new cl_device_id[]{device},
                null, null, null );

        //create a command-queue for selected device
        cl_command_queue commandQueue =
                clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for input  and output data
        cl_mem memObjects[] = new cl_mem[3];
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcA, null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcB, null);
        memObjects[2] = clCreateBuffer(context,
                 CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        // create the program from the source code
        cl_program  program = clCreateProgramWithSource(context,
                1, new String[]{ programSource }, null, null);

        //build the program
        clBuildProgram(program, 0, null, null, null, null);

        // create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);

        //set arguments for the kernel
        clSetKernelArg(kernel, 0,
                Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
                Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2,
                Sizeof.cl_mem, Pointer.to(memObjects[2]));

        //set the work-item dimensions
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};

        long startComputation = System.currentTimeMillis();
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);

        //read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                n * Sizeof.cl_float, dst, 0, null, null);
        System.out.println("Computation took in: "+ (System.currentTimeMillis() - startComputation) + " ms.");
        //release the kernel, program and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        System.out.println("Whole process took in: "+ (System.currentTimeMillis() - start) + " ms.");
        //verify the result
    }
    private static String getString(cl_device_id device, int paramName){
        //obtain the length of the String that will be queried
         long size[] = new long[1];
         clGetDeviceInfo(device, paramName, 0, null, size);

        //create a buffer of the appropriate size and  fill it with  the info
        byte buffer[] = new byte[(int) size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        return new String(buffer);
    }
}
