/*******************************************************************************
 * Copyright (c) 2021 StingoDL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package stingodl;

public class BitUtil {

    private static int[][] bitMasks = new int[][]
            {{0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x01}, // 1 bit
                    {0xc0,0x60,0x30,0x18,0x0c,0x06,0x03}, // 2 bits
                    {0xe0,0x70,0x38,0x1c,0x0e,0x07}, // 3bits
                    {0xf0,0x78,0x3c,0x1e,0x0f}, // 4 bits
                    {0xf8,0x7c,0x3e,0x1f}, // 5 bits
                    {0xfc,0x7e,0x3f}, //6 bits
                    {0xfe,0x7f} // 7 bits
            };

    public static boolean bool(byte[] data, int byteOffset, int bitOffset) {
        int mask = bitMasks[0][bitOffset];
        return (data[byteOffset] & mask) == mask;
    }

    public static int uint(byte[] data, int byteOffset, int bitOffset, int length) {
        if (length > 31) {
            throw new RuntimeException("Length too long for Int");
        }
        int bits = bitOffset + length;
        if (bits <= 8) {
            return singleByte(data[byteOffset] & 0xff, bitOffset, length);
        }
        int componentIndex = bits / 8;
        if ((bits % 8) == 0) {
            componentIndex--;
        }
        int leftShift = 0;
        int result = 0;
        int temp = 0;
        int compLength = 0;
        for (int i = componentIndex; i >= 0; i-- ) {
            if (i == 0) { // now look at bitOffset
                temp = singleByte(data[byteOffset] & 0xff, bitOffset, 8 - bitOffset);
            } else {
                if (i == componentIndex) {
                    compLength = bits - (i * 8);
                } else {
                    compLength = 8;
                }
                temp = singleByte(data[byteOffset + i] & 0xff, 0, compLength);
            }
            result = result | (temp << leftShift);
            leftShift += compLength;
        }
        return result;
    }

    public static long ulong(byte[] data, int byteOffset, int bitOffset, int length) {
        int bits = bitOffset + length;
        if (length <= 31) {
            return (long)uint(data, byteOffset, bitOffset, length);
        }
        int componentIndex = bits / 8;
        if ((bits % 8) == 0) {
            componentIndex--;
        }
        int leftShift = 0;
        long result = 0;
        long temp = 0;
        int compLength = 0;
        for (int i = componentIndex; i >= 0; i-- ) {
            if (i == 0) { // now look at bitOffset
                temp = (long)singleByte(data[byteOffset] & 0xff, bitOffset, 8 - bitOffset);
            } else {
                if (i == componentIndex) {
                    compLength = bits - (i * 8);
                } else {
                    compLength = 8;
                }
                temp = (long)singleByte(data[byteOffset + i] & 0xff, 0, compLength);
            }
            result = result | (temp << leftShift);
            leftShift += compLength;
        }
        return result;
    }

    private static int singleByte(int data, int bitOffset, int length) {
        if (length == 8) {
            return data;
        } else {
            int shift = (8 - bitOffset - length);
            if (shift == 0) {
                return data & bitMasks[length - 1][bitOffset];
            } else {
                return (data & bitMasks[length - 1][bitOffset]) >>> shift;
            }
        }
    }
}
