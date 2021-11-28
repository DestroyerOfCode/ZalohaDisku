package operacnesystemy.uloha2.service;

import operacnesystemy.uloha2.data.BlockType;
import operacnesystemy.uloha2.data.DataBlock;
import operacnesystemy.uloha2.data.Disk;
import operacnesystemy.uloha2.data.IndexNode;

import static operacnesystemy.uloha2.service.DiskService.blockSize;
import static operacnesystemy.uloha2.service.DiskService.blocksCount;

public class IndexNodeService {

    private static final int NUMBER_OF_SPACES = 2;

    public Integer findIndexNodeBlockNumber(String filename, Disk disk) {
        int i;
        for (i = 0; i < blocksCount; ++i) {
            if (disk.getBlocks().get(i).getContent().startsWith(filename, 0)) {
                return i;
            }
        }
        throw new RuntimeException("File not found");
    }

    public int find_free_block(int from_index, Disk disk) {
        int j;
        for (j = from_index; j < blocksCount; j++) {
            if (disk.getBlocks().get(j).getType() == BlockType.FREE) {
                return j;
            }
        }

        throw new RuntimeException("Free block not found");
    }

    public void setDataBlocksInNodes(IndexNode indexNode, String content, int dataBlock1, int dataBlock2) {
        indexNode.setBlock("i");
        for (int i = 1; i < blockSize; ++i) {
            indexNode.setBlock(indexNode.getBlock() + "-");
        }
        indexNode.setBlock("i" + indexNode.getFilename() + " " + dataBlock1 + " " + dataBlock2 +
                getDashCount(indexNode, dataBlock1, dataBlock2));

        indexNode.setFirstDirect(new DataBlock("d" + content.substring(0, Math.min(content.length(), blockSize))));
        if (content.length() > 32) {
            indexNode.setSecondIndirect(new DataBlock("d" + content.substring(blockSize, Math.min(content.length(), blockSize * 2))));
        }
    }

    private String getDashCount(IndexNode indexNode, int dataBlock1, int dataBlock2) {
        return indexNode.getBlock().substring(indexNode.getFilename().length() + Integer.toString(dataBlock1).length() +
                Integer.toString(dataBlock2).length() + NUMBER_OF_SPACES);
    }
}
