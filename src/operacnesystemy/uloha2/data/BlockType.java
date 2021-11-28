package operacnesystemy.uloha2.data;

import java.util.HashMap;
import java.util.Map;

public enum BlockType {

    INODE("i"),
    DATA("d"),
    FREE("f");

    public static Map<String, BlockType> valueKeyMap = new HashMap<>(){{
       put("i", INODE);
       put("d", DATA);
       put("f", FREE);
    }};
    private final String type;

    private BlockType(String type) {
        this.type = type;
    }
}
