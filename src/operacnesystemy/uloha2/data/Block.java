package operacnesystemy.uloha2.data;

public class Block {

    public Block() {
    }

    public Block(BlockType type, String content) {
        this.type = type;
        this.content = content;
    }

    BlockType type;
    String content;

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
