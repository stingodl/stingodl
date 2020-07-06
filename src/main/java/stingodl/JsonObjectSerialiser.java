/*******************************************************************************
 * Copyright (c) 2020 StingoDL.
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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class JsonObjectSerialiser {

    public static String toJson(Object o) throws Exception {
        return toJsonObject(o).toString();
    }

    public static void toJson(Object o, File f) throws Exception {
        JsonValue jv = toJsonObject(o);
        FileWriter fw = new FileWriter(f);
        jv.writeTo(fw);
        fw.close();
    }

    private static JsonObject toJsonObject(Object o) throws Exception {
        Class<?> oClass = o.getClass();
        JsonObject jo = new JsonObject();
        Field[] fields = oClass.getDeclaredFields();
        for (Field f: fields) {
            int mods = f.getModifiers();
            if (Modifier.isTransient(mods) || Modifier.isStatic(mods)) {
                // do not export transients and statics
            } else {
                Class<?> type = f.getType();
                if (type.equals(String.class)) {
                    if (f.get(o) != null) {
                        jo.add(f.getName(), f.get(o).toString());
                    }
                } else if (type.equals(int.class)) {
                    jo.add(f.getName(), f.getInt(o));
                } else if (type.equals(long.class)) {
                    jo.add(f.getName(), f.getLong(o));
                } else if (type.equals(boolean.class)) {
                    jo.add(f.getName(), f.getBoolean(o));
                } else if (type.equals(List.class)) {
                    if (f.get(o) != null) {
                        jo.add(f.getName(), toJsonArray((List) f.get(o)));
                    }
                } else {
                    if (f.get(o) != null) {
                        jo.add(f.getName(), toJsonObject(f.get(o)));
                    }
                }
            }
        }
        return jo;
    }

    private static JsonArray toJsonArray(List list) throws Exception {
        JsonArray ja = new JsonArray();
        for (Object o: list) {
            if (o != null) {
                if (o instanceof String) {
                    ja.add(o.toString());
                } else if (o instanceof List) {
                    ja.add(toJsonArray((List) o));
                } else {
                    ja.add(toJsonObject(o));
                }
            }
        }
        return ja;
    }
}
