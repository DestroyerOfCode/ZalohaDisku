package operacnesystemy.uloha2.service;

import operacnesystemy.uloha2.data.Disk;

public class IndexNodeService {

    public Integer findIndexNode(String filename, Disk disk) {
        int i;
        for (i = 0; i < DiskService.blocksCount; ++i) {
            if (disk.getDisk().get(i).startsWith(filename, 1)) {
                return i;
            }
        }
        return i;
    }
}
