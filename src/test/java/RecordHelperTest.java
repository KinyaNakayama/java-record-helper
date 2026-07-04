import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecordHelperTest {

    // テスト用のRecord定義
    record User(
            String name,
            int age,
            String[] backgroundList
    ) {
        public User(
                String name,
                int age,
                String background
        ) {
            this(
                    name,
                    age,
                    new String[]{background}
            );
        }
    }

    static User original = new User(
            "田中",
            25,
            new String[]{
                    "〇〇小学校卒業",
                    "〇〇中学校卒業",
                    "〇〇高校卒業",
            }
    );

    @Test
    @DisplayName("正常系: 1つのフィールドだけを変更できること")
    void testWithSingleChange() {

        User updated = RecordHelper.with(
                original,
                Map.of(
                        "age", 26
                )
        );

        assertNotNull(updated);
        assertEquals(original.name, updated.name()); // 変更していない値は維持
        assertEquals(26, updated.age());      // 指定した値が変更されている
        assertEquals(original.backgroundList(), updated.backgroundList);
        assertNotSame(original, updated);     // 異なるインスタンスであること
    }

    @Test
    @DisplayName("正常系: 複数のフィールド（String, int, 配列）を同時に変更できること")
    void testWithMultipleChanges() {
        String[] newBackground = {"〇〇大学卒業"};

        User updated = RecordHelper.with(original, Map.of(
                "name", "佐藤",
                "age", 30,
                "backgroundList", newBackground
        ));

        assertNotNull(updated);
        assertEquals("佐藤", updated.name());
        assertEquals(30, updated.age());
        assertArrayEquals(newBackground, updated.backgroundList()); // 配列の変更検証
        assertNotSame(original, updated);
    }

    @Test
    @DisplayName("正常系: nullに変更できること")
    void testWithNullChanges() {
        User updated = RecordHelper.with(
                original,
                new HashMap<>() {{
                    put("name", null);
                    put("backgroundList", null);
                }}
        );

        assertNotNull(updated);
        assertNull(updated.name());
        assertEquals(original.age, updated.age());
        assertNull(updated.backgroundList());
        assertNotSame(original, updated);
    }

    @Test
    @DisplayName("準正常系: 元のRecordがnullの場合はnullを返すこと")
    void testWithNullRecord() {
        User updated = RecordHelper.with(null, Map.of("age", 30));
        assertNull(updated);
    }

    @Test
    @DisplayName("準正常系: 変更マップがnullまたは空の場合は元のインスタンスをそのまま返すこと")
    void testWithNullOrEmptyChanges() {
        assertSame(original, RecordHelper.with(original, null));
        assertSame(original, RecordHelper.with(original, Collections.emptyMap()));
    }

    @Test
    @DisplayName("異常系: 存在しないフィールド名が渡された場合、IllegalArgumentExceptionがスローされること")
    void testWithUnknownKeyThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            RecordHelper.with(original, Map.of(
                    "invalidField", "値"
            ));
        });

        assertTrue(exception.getMessage().contains("Contain unknown change key: invalidField"));
    }

    @Test
    @DisplayName("異常系: 存在するキーと存在しないキーが混在している場合、例外が発生して処理されないこと")
    void testWithMixedKeysThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecordHelper.with(original, Map.of(
                    "age", 40,
                    "unknownKey", "値"
            ));
        });
    }

    @Test
    @DisplayName("異常系: キーは一致しているが、データ型が不一致の場合に例外がスローされること")
    void testWithMismatchedDataTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecordHelper.with(original, Map.of(
                    "age",
                    "二十六")
            );
        });
    }

    @Test
    @DisplayName("異常系: プリミティブ型を null に置き換えようとした場合に例外がスローされること")
    void testWithPrimitiveDataNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecordHelper.with(
                    original,
                    new HashMap<>() {{
                        put("name", null);
                        put("age", null);
                        put("backgroundList", null);
                    }}
            );
        });
    }
}