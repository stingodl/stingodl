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

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;

public class TsDecryptInputStream extends InputStream {

    InputStream is;
    Cipher cipher;
    byte[] in = new byte[16];
    byte[] out = new byte[16];
    int lastReadLength = -1;
    int offset = 0;
    boolean encrypted = true;
    boolean atSyncByte = false;

    public TsDecryptInputStream(InputStream is, byte[] key, int ivInt) throws Exception {
        this.is = is;
        if (key == null) {
            encrypted = false;
        }
        if (encrypted) {
            if (key.length != 16) {
                System.out.println("Key " + new String(key));
            }
            byte[] iv = new byte[16];
            for (int i = 0; i < 14; i++) {
                iv[i] = 0;
            }
            iv[14] = (byte) (ivInt >> 8);
            iv[15] = (byte) ivInt;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            decryptBlock();
        }
    }

    private int decryptBlock() throws IOException {
        if (lastReadLength == 0) {
            return 0;
        }
        lastReadLength = is.readNBytes(in, 0, 16);
        if (lastReadLength > 0) {
            if (lastReadLength < 16) {
                throw new IOException("InputStream length not a multiple of 16");
            }
            try {
                cipher.update(in, 0, 16, out);
            } catch (ShortBufferException sbe) {
                throw new IOException("CipherUpdate failed", sbe);
            }
        } else { // at end
            try {
                out = cipher.doFinal();
//                System.out.println("DoFinal " + out.length);
            } catch (Exception ex) {
                throw new IOException("CipherDoFinal failed", ex);
            }
        }
        return out.length;
    }

    public void seekSyncByte(int syncByte) throws IOException {
        if (encrypted) {
            while (out[offset] != syncByte) {
//            System.out.println("Seek " + offset);
                if (offset < 15) {
                    offset++;
                } else {
                    offset = 0;
                    if (decryptBlock() == 0) {
                        throw new IOException("No Sync Byte found");
                    }
                }
            }
        } else {
            int b = is.read();
            while ((b != TsPacket.SYNC_BYTE) && (b >= 0)) {
                b = is.read();
            }
            if (b < 0) {
                throw new IOException("No Sync Byte found");
            } else {
                atSyncByte = true;
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (encrypted) {
            if (offset >= out.length) {
                if (decryptBlock() == 0) { //at end
                    return -1;
                } else {
                    offset = 0;
                }
            }
            return out[offset++] & 0xff;
        } else {
            if (atSyncByte) {
                atSyncByte = false;
                return TsPacket.SYNC_BYTE;
            } else {
                return is.read();
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        is.close();
    }
}
