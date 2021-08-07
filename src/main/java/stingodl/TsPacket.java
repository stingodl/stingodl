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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
    Transport Stream Packet
    Includes getters for Adaption Fields whether or not these are present, ie code must
    check for their presence before using getters. Similarly includes getters for PES
    headers.
 */
public class TsPacket {
    public static int PACKET_SIZE = 188;
    public static int NULL_PID = 0x1fff;
    static byte SYNC_BYTE = 0x47;
    byte[] data = new byte[PACKET_SIZE];
    TsPacket next;
    int adaptionTotalLength;
    int tsPayloadStart;
    int adaptionFieldControl;

    public TsPacket() {}

    public TsPacket(TsPacket tsp) {
        System.arraycopy(tsp.data, 0, this.data, 0, PACKET_SIZE);
        this.tsPayloadStart = tsp.tsPayloadStart;
        this.adaptionTotalLength = tsp.adaptionTotalLength;
    }

    public boolean isIsoReserved() {
        return adaptionFieldControl == 0;
    }

    public boolean isAdaptionFieldOnly() {
        return adaptionFieldControl == 2;
    }

    public boolean isAdaptionFieldPresent() {
        return adaptionTotalLength > 1;
        // looking at adaptionFieldControl is not enough as adaptionFieldLength
        // may be 0, which is effectively a one byte stuff
    }

    public int getTsPayloadStart() { return tsPayloadStart; }

    public byte[] getData() { return data; }

    public boolean isTransportError() { return BitUtil.bool(data, 1, 0); }

    public boolean isPayloadUnitStart() {
        return BitUtil.bool(data, 1, 1);
    }

    public boolean isTransportPriority() {
        return BitUtil.bool(data, 1, 2);
    }

    public int getPID() { return BitUtil.uint(data, 1, 3, 13); }

    public int getTransportScramblingControl() {
        return BitUtil.uint(data, 3, 0, 2);
    }

    public int getAdaptionFieldControl() { return adaptionFieldControl; }

    public int getContinuityCounter() { return BitUtil.uint(data, 3, 4, 4); }

    // Start Adaption Field getters

    public int getAdaptionFieldLength() {
        if (adaptionFieldControl < 2) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.uint(data, 4, 0, 8);
    }

    public boolean isDiscontinuity() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 0);
    }

    public boolean isRandomAccess() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 1);
    }

    public boolean isElementaryStreamPriority() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 2);
    }

    public boolean isPCR() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 3);
    }

    public boolean isOPCR() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 4);
    }

    public boolean isSplicingPoint() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 5);
    }

    public boolean isTransportPrivateData() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 6);
    }

    public boolean isAdaptionFieldExtension() {
        if (!isAdaptionFieldPresent()) {
            throw new NoAdaptionFieldException();
        }
        return BitUtil.bool(data, 5, 7);
    }

    public long getPCRBase() {
        if (isAdaptionFieldPresent() && isPCR()) {
            return ((BitUtil.uint(data, 6, 0, 32) & 0xffffffffL) << 1) |
                    ((long)BitUtil.uint(data, 10, 0, 1));
        } else {
            throw new NoAdaptionFieldException();
        }
    }

    // Start PES packet headers

    public int getStartCodePrefix() {
        return BitUtil.uint(data, 4 + adaptionTotalLength, 0, 24);
    }

    public int getStreamId() {
        return BitUtil.uint(data, 7 + adaptionTotalLength, 0, 8);
    }

    public int getPesPacketLength() {
        return BitUtil.uint(data, 8 + adaptionTotalLength, 0, 16);
    }

    public int getPesScramblingControl() {
        return BitUtil.uint(data, 10 + adaptionTotalLength, 2, 2);
    }

    public boolean isPesPriority() {
        return BitUtil.bool(data, 10 + adaptionTotalLength, 4);
    }

    public boolean isDataAlignment() {
        return BitUtil.bool(data, 10 + adaptionTotalLength, 5);
    }

    public boolean isCopyright() {
        return BitUtil.bool(data, 10 + adaptionTotalLength, 6);
    }

    public boolean isOriginalOrCopy() {
        return BitUtil.bool(data, 10 + adaptionTotalLength, 7);
    }

    public boolean isPts() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 0);
    }

    public boolean isDts() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 1);
    }

    public boolean isEscr() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 2);
    }

    public boolean isEsRate() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 3);
    }

    public boolean isDsmTrickMode() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 4);
    }

    public boolean isAdditionCopy() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 5);
    }

    public boolean isPesCrc() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 6);
    }

    public boolean getPesExtension() {
        return BitUtil.bool(data, 11 + adaptionTotalLength, 7);
    }

    public int getPesHeaderDataLength() {
        return BitUtil.uint(data, 12 + adaptionTotalLength, 0, 8);
    }

    private long getTimestampLong(int offset) {
        int b32_30 = BitUtil.uint(data, offset, 4, 3);
        int b29_15 = BitUtil.uint(data, offset + 1, 0, 15);
        int b14_0 = BitUtil.uint(data, offset + 3, 0, 15);
        return (((long)b32_30) << 30) | (((long)b29_15) << 15) | ((long)b14_0);
    }

    private int getTimestamp(int offset) {
        int b30 = BitUtil.uint(data, offset, 6, 1);
        int b29_15 = BitUtil.uint(data, offset + 1, 0, 15);
        int b14_0 = BitUtil.uint(data, offset + 3, 0, 15);
        return (b30 << 30) | (b29_15 << 15) | b14_0;
    }

    public int getPts() {
        return getTimestamp(13 + adaptionTotalLength);
    }

    public int getDts() {
        return getTimestamp(18 + adaptionTotalLength);
    }

    public int loadData(InputStream is) throws IOException {
        int size = is.readNBytes(data, 0, PACKET_SIZE);
        if (size == 0) { // at end
            return 0;
        }
        if (data[0] != SYNC_BYTE) {
            throw new IOException("Invalid Sync Byte: " + data[0]);
        }
        if (size != PACKET_SIZE) {
            throw new IOException("Short packet: " + size + " sync " + data[0]);
        }
        if (isTransportError()) {
            throw new IOException("Transport Error Indicator set");
        }
        /*
        continuityCounter = (data[3] & 0x0000000f);
        int nextContinuityCounter = 0;
        if (isIsoReserved() || isAdaptionFieldOnly() || (pid == NULL_PID)) { // do not increment
            nextContinuityCounter = expectedContinuityCounter;
        } else if (continuityCounter == expectedContinuityCounter) {
            nextContinuityCounter = (continuityCounter + 1) & 0x0000000f;
        } else {
            throw new IOException("Packet discontinuity. Expected: " + expectedContinuityCounter
            + " Found: " + continuityCounter);
        }
         */
        // packet verified OK
        adaptionFieldControl = BitUtil.uint(data, 3, 2, 2);
        if (adaptionFieldControl == 1) { // No adaption field
            adaptionTotalLength = 0;
            tsPayloadStart = 4;
        } else if (adaptionFieldControl == 2) { // No payload
            adaptionTotalLength = (1 + getAdaptionFieldLength());
            tsPayloadStart = PACKET_SIZE;
        } else if (adaptionFieldControl == 3) { // Adaption field and payload
            adaptionTotalLength = (1 + getAdaptionFieldLength());
            tsPayloadStart = (5 + getAdaptionFieldLength());
        }
        return PACKET_SIZE;
    }

    public int getPesPayloadStart() throws IOException {
        int pesPayloadStart = 13 + adaptionTotalLength + getPesHeaderDataLength();
        if (pesPayloadStart > PACKET_SIZE) {
            throw new IOException("PES payload start is beyond this packet");
        }
        return pesPayloadStart;
    }

    public void writePayload(OutputStream os) throws IOException {
        if (tsPayloadStart < PACKET_SIZE) {
            os.write(data, tsPayloadStart, (PACKET_SIZE - tsPayloadStart));
        }
    }

    public TsPacket getNext() {
        return next;
    }

    public void setNext(TsPacket next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object obj) {
        TsPacket that = (TsPacket) obj;
        for (int i = 0; i < PACKET_SIZE; i++) {
            if (i == 3) { // includes continuity counter which needs to be ignored
                if (BitUtil.uint(this.data,3, 0, 4) !=
                        BitUtil.uint(that.data,3, 0, 4)) {
                    return false;
                }
            } else if (this.data[i] != that.data[i]) {
                return false;
            }
        }
        return true;
    }

    public String getPayloadAsString() {
        byte[] payload = new byte[PACKET_SIZE - tsPayloadStart];
        System.arraycopy(data, tsPayloadStart, payload, 0, PACKET_SIZE - tsPayloadStart);
        return new String(payload);
    }
}
