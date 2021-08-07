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

import com.eclipsesource.json.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JsonConstructiveParser extends com.eclipsesource.json.JsonHandler {

    private LinkedList<Level> stack = new LinkedList<>();
    private Level first;
    private Object o;

    private JsonConstructiveParser(Class<?> baseClass) {
        first = new Level();
        first.oClass = baseClass;
        try {
            first.o = first.oClass.getConstructor().newInstance();
        } catch (Exception e) {}
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(Reader reader, Class<T> type) throws IOException {
        JsonConstructiveParser handler = new JsonConstructiveParser(type);
        JsonParser parser = new JsonParser(handler);
        parser.parse(reader);
        reader.close();
        return (T)handler.o;
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String s, Class<T> type) {
        JsonConstructiveParser handler = new JsonConstructiveParser(type);
        JsonParser parser = new JsonParser(handler);
        parser.parse(s);
        return (T)handler.o;
    }

    @Override
    public void startNull() {
    }

    @Override
    public void endNull() {
    }

    @Override
    public void startBoolean() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endBoolean(boolean value) {
        Level l = stack.getFirst();
        try {
            if (l.isArray) {
                if (l.o != null) {
                    ((List)l.o).add(value);
                }
            } else {
                if (l.field != null) {
                    l.field.setBoolean(l.o, value);
                    l.field = null;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public void startString() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endString(String string) {
        Level l = stack.getFirst();
        try {
            if (l.isArray) {
                if (l.o != null) {
                    ((List)l.o).add(string);
                }
            } else {
                if (l.field != null) {
                    l.field.set(l.o, string);
                    l.field = null;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public void startNumber() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endNumber(String string) {
        Level l = stack.getFirst();
        try {
            if (l.isArray) {
                if (l.o != null) {
                    if (l.arrayClass.equals(Integer.class)) {
                        ((List)l.o).add(Integer.decode(string));
                    } else if (l.arrayClass.equals(Long.class)) {
                        ((List)l.o).add(Long.decode(string));
                    }
                }
            } else {
                if (l.field != null) {
                    Class<?> num = l.field.getType();
                    if (num.equals(int.class)) {
                        l.field.setInt(l.o, Integer.parseInt(string));
                    } else if (num.equals(long.class)) {
                        l.field.setLong(l.o, Long.parseLong(string));
                    }
                    l.field = null;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public Object startArray() {
        Level l = stack.getFirst();
        try {
            Level newL = new Level();
            newL.isArray = true;
            if (l.field != null) {
                newL.oClass = l.field.getType();
                if (newL.oClass.equals(List.class)) {
                    ParameterizedType pt = (ParameterizedType)l.field.getGenericType();
                    newL.arrayClass = (Class<?>)pt.getActualTypeArguments()[0];
                    newL.o = new ArrayList<>();
                }
            }
            stack.addFirst(newL);
        } catch (Exception e) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endArray(Object array) {
        Level l = stack.removeFirst();
        try {
            if (stack.isEmpty()) {
                this.o = l.o;
            } else {
                Level prev = stack.getFirst();
                if (prev.isArray) {
                    if (prev.o != null) {
                        ((List)prev.o).add(l.o);
                    }
                } else {
                    if (prev.field != null) {
                        prev.field.set(prev.o, l.o);
                        prev.field = null;
                    }
                }
            }
        } catch (Exception e) {}
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startArrayValue(Object array) {
        super.startArrayValue(array);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endArrayValue(Object array) {
        super.endArrayValue(array);
    }

    @Override
    public Object startObject() {
        if (stack.isEmpty()) {
            stack.addFirst(first);
        } else {
            Level l = stack.getFirst();
            try {
                Level newL = new Level();
                if (l.isArray) {
                    if (l.o != null) {
                        newL.oClass = l.arrayClass;
                        newL.o = newL.oClass.getConstructor().newInstance();
                    }
                } else {
                    if (l.field != null) {
                        newL.oClass = l.field.getType();
                        newL.o = newL.oClass.getConstructor().newInstance();
                    }
                }
                stack.addFirst(newL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void endObject(Object object) {
        Level l = stack.removeFirst();
        try {
            if (stack.isEmpty()) {
                this.o = l.o;
            } else {
                Level prev = stack.getFirst();
                if (prev.isArray) {
                    if (prev.o != null) {
                        ((List)prev.o).add(l.o);
                    }
                } else {
                    if (prev.field != null) {
                        prev.field.set(prev.o, l.o);
                        prev.field = null;
                    }
                }
           }
        } catch (Exception e) {}
    }

    @Override
    public void startObjectName(Object object) {
    }

    @Override
    public void endObjectName(Object object, String name) {
        String fixed = null;
        if (name.equals("double")) {
            fixed = "double_";
        } else if (name.equals("720")) {
            fixed = "hd";
        } else {
            fixed = name.replace('-','_');
        }
        Level l = stack.getFirst();
        try {
            if (l.oClass != null) {
                l.field = l.oClass.getDeclaredField(fixed);
            }
        } catch (Exception e) {
            l.field = null;
        }
    }

    @Override
    public void startObjectValue(Object object, String name) {
    }

    @Override
    public void endObjectValue(Object object, String name) {
    }

    public static class Level {
        public Class<?> oClass;
        public Object o;
        public Field field;
        public boolean isArray = false;
        public Class<?> arrayClass;
        public String toString() {
            return "Level: " + oClass + " " + o + " " + field + " " + isArray + " " + arrayClass;
        }
    }
}
