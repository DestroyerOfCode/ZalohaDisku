package operacnesystemy.uloha2.service;

import operacnesystemy.uloha2.Commands;
import operacnesystemy.uloha2.data.Disk;
import operacnesystemy.uloha2.data.IndexNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import static operacnesystemy.uloha2.Commands.EXIT;
import static operacnesystemy.uloha2.Commands.MANUAL;

public class DiskService {

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
            int blockStart = i * blockSize;
            int blockEnd = (i * blockSize) + blockSize;
            disk.getDisk().add(i, read.substring(blockStart, blockEnd));
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
            System.out.println("Enter Filename: ");

            IndexNode indexNode = new IndexNode();
            indexNode.setFilename(new Scanner(System.in).nextLine());

            String fileContent = createFileContent();
            indexNode.setBlockNumber(indexNodeService.find_free_block(0, disk));

            if (indexNode.getBlockNumber() >= blocksCount) {
                throw new RuntimeException("Disk is full");
            }

            int dataBlock1 = indexNodeService.find_free_block(indexNode.getBlockNumber() + 1, disk);
            int dataBlock2 = indexNodeService.find_free_block(dataBlock1 + 1, disk);

            indexNodeService.setDataBlocksInNodes(indexNode, fileContent, dataBlock1, dataBlock2);

            fillDiskWithNewBlocks(indexNode, dataBlock1, dataBlock2);
        };
    }

    private void fillDiskWithNewBlocks(IndexNode indexNode, int dataBlock1, int dataBlock2) {
        disk.getDisk().set(indexNode.getBlockNumber(), indexNode.getBlock());
        disk.getDisk().set(dataBlock1, indexNode.getFirstDirect().getData());

        if (dataBlock2 < blocksCount) {
            disk.getDisk().set(dataBlock2, indexNode.getSecondIndirect().getData());
        }
    }

    private String createFileContent() {
        String content = new Scanner(System.in).next().split("-")[0];
        while (content.length() < blockSize * 2) {
            content = content.concat("-");
        }
        return content;
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
                .substring(1, getLastCharacterOfBlockOrDash(blockNumber))
        );
    }

    private int getLastCharacterOfBlockOrDash(int blockNumber) {
        return disk.getDisk().get(blockNumber).indexOf('-') == -1 ?
                disk.getDisk().get(blockNumber).length() :
                disk.getDisk().get(blockNumber).indexOf('-');
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
