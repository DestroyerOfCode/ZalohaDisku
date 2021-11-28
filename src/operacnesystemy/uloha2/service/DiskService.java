package operacnesystemy.uloha2.service;

import operacnesystemy.uloha2.Commands;
import operacnesystemy.uloha2.data.DataBlock;
import operacnesystemy.uloha2.data.Disk;
import operacnesystemy.uloha2.data.IndexNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import static operacnesystemy.uloha2.Commands.EXIT;
import static operacnesystemy.uloha2.Commands.MANUAL;

public class DiskService {

    private static final int NUMBER_OF_SPACES = 2;

    public DiskService(Disk disk, IndexNodeService indexNodeService) {
        this.disk = disk;
        this.indexNodeService = indexNodeService;
    }

    public static final int blocksCount = 8;
    public static final int blockSize = 32;
    public static final String MANUAL_TEXT = """
            List of commands
            format - format disk
            display_disk - show disk contents on the screen
            display_file - show file contents on the screen
            write - write a new file to disk
            manual - this manual :
            exit - exit""";

    private final IndexNodeService indexNodeService;
    private final Disk disk;

    public void start() {
        try {
            String read = readFile();
            terminal();
            writeToOutputFile(read, "src/operacnesystemy/uloha2/resources/uloha2-zaloha_disku.txt");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    private void writeToOutputFile(String read, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(read.getBytes(StandardCharsets.UTF_8));
    }

    private String readFile() throws IOException {
        Path path = Paths.get("src/operacnesystemy/uloha2/resources/uloha2-zaloha_disku.txt");
        String read = Files.readAllLines(path).get(0);
        for (int i = 0; i < blocksCount; ++i) {
            disk.getDisk().add(i, read.substring(i * blockSize, (i * blockSize) + blockSize));
        }
        return read;
    }

    private void terminal() {
        Commands command = MANUAL;
        do {
            try {
                switch (command) {
                    case FORMAT -> format().run();
                    case DISPLAY_DISK -> displayDisk().run();
                    case DISPLAY_FILE -> displayFile().run();
                    case WRITE -> writeFile().run();
                    case MANUAL -> System.out.println(MANUAL_TEXT);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            try {
                command = getCommand();
            } catch (IllegalArgumentException e) {
                command = MANUAL;
                e.printStackTrace();
            }

        } while (command != EXIT);
    }

    private Runnable writeFile() {

        return () -> {
            System.out.println("Filename: ");

            IndexNode indexNode = new IndexNode();
            indexNode.setFilename(new Scanner(System.in).nextLine());

            String content = createContent();
            indexNode.setBlockNumber(find_free_block(0));
            if (indexNode.getBlockNumber() >= blocksCount) {
                throw new RuntimeException("Disk is full");
            }

            int dataBlock1 = find_free_block(indexNode.getBlockNumber() + 1);
            int dataBlock2 = find_free_block(dataBlock1 + 1);

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

            disk.getDisk().set(indexNode.getBlockNumber(), indexNode.getBlock());
            disk.getDisk().set(dataBlock1, indexNode.getFirstDirect().getData());

            if (dataBlock2 < blocksCount) {
                disk.getDisk().set(dataBlock2, indexNode.getSecondIndirect().getData());
            }
        };
    }

    private String getDashCount(IndexNode indexNode, int dataBlock1, int dataBlock2) {
        return indexNode.getBlock().substring(indexNode.getFilename().length() + Integer.toString(dataBlock1).length() +
                Integer.toString(dataBlock2).length() + NUMBER_OF_SPACES);
    }

    private String createContent() {
        StringBuilder content = new StringBuilder(new Scanner(System.in).next());
        content = new StringBuilder(content.substring(0, content.indexOf("-")));
        while (content.length() < blockSize * 2) {
            content.append("-");
        }
        return new String(content);
    }

    private int find_free_block(int from_index) {
        int j;
        for (j = from_index; j < blocksCount; j++) {
            if (disk.getDisk().get(j).charAt(0) == 'f') {
                return j;
            }
        }

        return j;
    }

    private Runnable displayFile() {

        return () -> {
            System.out.println("filename: ");

            IndexNode iNode = createIndexNode();

            printDataBlocks(iNode);
        };
    }

    private void printDataBlocks(IndexNode iNode) {
        if (blocksCount - 1 >= (iNode.getBlockNumber() + 1)) {
            printBlock(iNode.getBlockNumber() + 1);
        }

        if (blocksCount - 1 >= (iNode.getBlockNumber() + 2)) {
            printBlock(iNode.getBlockNumber() + 2);
        }
    }

    private IndexNode createIndexNode() {
        IndexNode iNode = new IndexNode();
        Scanner sc = new Scanner(System.in);

        iNode.setFilename(sc.nextLine());
        iNode.setBlockNumber(indexNodeService.findIndexNode(iNode.getFilename(), disk));
        iNode.setBlock(disk.getDisk().get((iNode.getBlockNumber())));

        return iNode;
    }

    private void printBlock(int blockNumber) {
        System.out.println(disk.getDisk().get(blockNumber)
                .substring(1, disk.getDisk().get(blockNumber).indexOf('-') == -1 ?
                        disk.getDisk().get(blockNumber).length() :
                        disk.getDisk().get(blockNumber).indexOf('-'))
        );
    }

    private Runnable format() {

        return () -> {
            String newBlock = "f" + "-".repeat(blockSize - 1);
            disk.setDisk(disk.getDisk().stream().map((String block) -> newBlock).collect(Collectors.toList()));
        };
    }

    private Runnable displayDisk() {
        return () -> {
            for (int i = 0; i < disk.getDisk().size(); i++) {
                System.out.printf("""
                        block %d
                        [%s]
                        """, i, disk.getDisk().get(i));
            }
        };
    }

    private Commands getCommand() {
        Scanner sc = new Scanner(System.in);
        return Commands.valueOf(sc.nextLine().toUpperCase(Locale.ROOT));
    }

}
