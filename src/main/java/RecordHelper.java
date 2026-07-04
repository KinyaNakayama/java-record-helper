import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;

public class RecordHelper {

    private RecordHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T with(
            T record,
            Map<String, Object> changes
    ) {
        if (record == null) {
            return null;
        }
        if (changes == null || changes.isEmpty()) {
            return record;
        }

        final var clazz = record.getClass();
        final var components = clazz.getRecordComponents();
        final var paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class[]::new);

        // 存在しないキーが含まれていないかチェック
        for (String key : changes.keySet()) {
            boolean exists = Arrays.stream(components).anyMatch(
                    c -> c.getName().equals(key)
            );
            if (!exists) {
                throw new IllegalArgumentException("Contain unknown change key: " + key);
            }
        }

        // コンストラクタ引数を作成
        Object[] args = Arrays.stream(components).map(c -> {
            if (changes.containsKey(c.getName())) {
                return changes.get(c.getName());
            }
            try {
                return c.getAccessor().invoke(record);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toArray();

        // オブジェクトを作成
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
            return (T) constructor.newInstance(args);
        } catch (InvocationTargetException |
                 NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}