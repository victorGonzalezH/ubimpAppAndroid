package com.metricsfab.utils.types;

import java.nio.ByteBuffer;

public class PrimitiveDataUtils {

    public static final int SIZE_OF_DOUBLE = 8;

    public static final int SIZE_OF_FLOAT = 4;

    public static final int SIZE_OF_INT = 4;

    /**
     * Convierte un valor double a un arreglo de bytes.
     * @param paramDouble
     * @return El arreglo de byes que representa el valor double
     */
    public static byte[] ConvertDoubleToByteArray(double paramDouble) { return ByteBuffer.allocate(SIZE_OF_DOUBLE).putDouble(paramDouble).array(); }

    /**
     * Convierte un valor flotante a un arreglo de bytes
     * @param paramFloat
     * @return El arreglo de bytes que representa el valor flotante
     */
    public static byte[] ConvertFloatToByteArray(float paramFloat) { return ByteBuffer.allocate(SIZE_OF_FLOAT).putFloat(paramFloat).array(); }

}
