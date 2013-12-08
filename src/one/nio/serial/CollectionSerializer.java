package one.nio.serial;

import one.nio.serial.gen.StubGenerator;

import java.io.ObjectInput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class CollectionSerializer extends Serializer<Collection> {
    private Constructor constructor;

    CollectionSerializer(Class cls) {
        super(cls);
        this.constructor = findConstructor();
        this.constructor.setAccessible(true);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String className = super.tryReadExternal(in, (Repository.stubOptions & Repository.COLLECTION_STUBS) == 0);
        if (this.cls == null) {
            this.cls = StubGenerator.generateRegular(className, "java.util.ArrayList", null);
            this.original = true;
        }

        this.constructor = findConstructor();
        this.constructor.setAccessible(true);
    }

    @Override
    public void calcSize(Collection obj, CalcSizeStream css) throws IOException {
        css.count += 4;
        for (Object v : obj) {
            css.writeObject(v);
        }
    }

    @Override
    public void write(Collection obj, DataStream out) throws IOException {
        out.writeInt(obj.size());
        for (Object v : obj) {
            out.writeObject(v);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection read(DataStream in) throws IOException, ClassNotFoundException {
        Collection result;
        try {
            result = (Collection) constructor.newInstance();
            in.register(result);
        } catch (Exception e) {
            throw new IOException(e);
        }

        int length = in.readInt();
        for (int i = 0; i < length; i++) {
            result.add(in.readObject());
        }
        return result;
    }

    @Override
    public void toJson(Collection obj, StringBuilder builder) throws IOException {
        builder.append('[');
        Iterator iterator = obj.iterator();
        if (iterator.hasNext()) {
            Json.appendObject(builder, iterator.next());
            while (iterator.hasNext()) {
                builder.append(',');
                Json.appendObject(builder, iterator.next());
            }
        }
        builder.append(']');
    }

    @SuppressWarnings("unchecked")
    private Constructor findConstructor() {
        try {
            return cls.getConstructor();
        } catch (NoSuchMethodException e) {
            Class implementation;
            if (SortedSet.class.isAssignableFrom(cls)) {
                implementation = TreeSet.class;
            } else if (Set.class.isAssignableFrom(cls)) {
                implementation = HashSet.class;
            } else if (Queue.class.isAssignableFrom(cls)) {
                implementation = LinkedList.class;
            } else {
                implementation = ArrayList.class;
            }
            
            try {
                return implementation.getDeclaredConstructor();
            } catch (NoSuchMethodException e1) {
                return null;
            }
        }
    }
}
