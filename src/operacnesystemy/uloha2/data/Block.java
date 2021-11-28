package operacnesystemy.uloha2.data;

import java.util.ArrayList;
import java.util.List;

public class Block {

    public Block() {
    }

    public Block(BlockType type, String content) {
        this.type = type;
        this.content = content;
    }

    private BlockType type;
    private String content;
    private Boolean isDeletable;
    private List<Integer> dataBlockNumbers = new ArrayList<>();

    public List<Integer> getDataBlockNumbers() {
        return dataBlockNumbers;
    }

    public void setDataBlockNumbers(List<Integer> dataBlockNumbers) {
        this.dataBlockNumbers = dataBlockNumbers;
    }

    public Boolean getDeletable() {
        return isDeletable;
    }

    public void setDeletable(Boolean deletable) {
        isDeletable = deletable;
    }

    public BlockType getType() {
        return type;
    }

    public void setType(BlockType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
