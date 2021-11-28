package operacnesystemy.uloha2.data;

public class IndexNode {

    String filename;
    String block;
    Integer blockNumber;
    DataBlock firstDirect;
    DataBlock secondIndirect;
    Integer time;

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DataBlock getFirstDirect() {
        return firstDirect;
    }

    public void setFirstDirect(DataBlock firstDirect) {
        this.firstDirect = firstDirect;
    }

    public DataBlock getSecondIndirect() {
        return secondIndirect;
    }

    public void setSecondIndirect(DataBlock secondIndirect) {
        this.secondIndirect = secondIndirect;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
