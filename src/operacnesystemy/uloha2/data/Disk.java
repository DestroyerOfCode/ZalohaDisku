package operacnesystemy.uloha2.data;

import java.util.List;

public class Disk {

    private List<Block> blocks;

    public Disk(List<Block> blocks) {
        this.blocks = blocks;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }
}
