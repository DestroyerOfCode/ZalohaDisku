package operacnesystemy.uloha2.service;

import operacnesystemy.uloha2.Commands;
import operacnesystemy.uloha2.data.Block;
import operacnesystemy.uloha2.data.BlockType;
import operacnesystemy.uloha2.data.Disk;
import operacnesystemy.uloha2.data.IndexNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static operacnesystemy.uloha2.Commands.EXIT;
import static operacnesystemy.uloha2.Commands.MANUAL;
import static operacnesystemy.uloha2.data.BlockType.valueKeyMap;

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
            delete - delete a file from disk
            grep - enter filename and sought word to print line
            delete_mode - make file deletable or not
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
            disk.getBlocks().add(i, new Block(valueKeyMap.get(Character.toString(read.charAt(blockStart))), read.substring(blockStart, blockEnd)));
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
                    case DELETE -> deleteFile().run();
                    case GREP -> grepFile().run();
                    case DELETE_MODE -> makeDeletable().run();
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

    private Runnable makeDeletable() {
        return () -> {
            String fileName = new Scanner(System.in).next();
            Integer blockNumber = indexNodeService.findIndexNode(fileName, disk);

        };
    }

    private Runnable grepFile() {

        return () -> {
            System.out.println("Filename and word: ");
            String command = new Scanner(System.in).nextLine();
            String fileName = command.split(" ")[0];
            String soughtWord = command.split(" ")[1];
            List<String> rows = new ArrayList<>();

            Integer blockNumber = indexNodeService.findIndexNode(fileName, disk);
            if (blockNumber + 1 < blocksCount) {
                rows.addAll(
                        Arrays.stream(disk.getBlocks()
                                        .get(blockNumber + 1)
                                        .getContent()
                                        .split("\n"))
                                .filter(row -> row.contains(soughtWord))
                                .collect(Collectors.toList()));
            }
            if (blockNumber + 1 < blocksCount) {
                rows.addAll(
                        Arrays.stream(disk.getBlocks()
                                        .get(blockNumber + 2)
                                        .getContent()
                                        .split("\n"))
                                .filter(row -> row.contains(soughtWord))
                                .collect(Collectors.toList()));
            }

            rows.forEach(System.out::println);
        };
    }

    private Runnable deleteFile() {

        return () -> {
            System.out.println("Filename: ");
            Scanner sc = new Scanner(System.in);

            Integer blockNumber = indexNodeService.findIndexNode(sc.next(), disk);
            disk.getBlocks().set(blockNumber, new Block(BlockType.FREE, "-".repeat(blockSize - 1)));

            if (blockNumber + 1 < blocksCount) {
                disk.getBlocks().set(blockNumber + 1, new Block(BlockType.FREE, "-".repeat(blockSize - 1)));
            }
            if (blockNumber + 2 < blocksCount) {
                disk.getBlocks().set(blockNumber + 2, new Block(BlockType.FREE, "-".repeat(blockSize - 1)));
            }
        };
    }

    private Runnable writeFile() {

        return () -> {
            System.out.println("Enter Filename: ");

            IndexNode indexNode = new IndexNode();
            indexNode.setFilename(new Scanner(System.in).nextLine());

            String fileContent = createFileContent();
            indexNode.setBlockNumber(indexNodeService.find_free_block(0, disk));
            indexNode.setTime(LocalDateTime.now());

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
        disk.getBlocks().set(indexNode.getBlockNumber(), new Block(BlockType.INODE, indexNode.getBlock()));
        disk.getBlocks().set(dataBlock1, new Block(BlockType.DATA, indexNode.getFirstDirect().getData()));

        if (dataBlock2 < blocksCount) {
            disk.getBlocks().set(dataBlock2, new Block(BlockType.DATA, indexNode.getSecondIndirect().getData()));
        }
    }

    private String createFileContent() {

        String content = setFileContent().split("-")[0];

        while (content.length() < blockSize * 2) {
            content = content.concat("-");
        }
        return content;
    }

    private String setFileContent() {
        String content = "";
        try {
            char character;
            do {
                character = (char) System.in.read();
                content = content.concat(Character.toString(character));
            } while (character != '-');
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
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
        iNode.setBlock(disk.getBlocks().get((iNode.getBlockNumber())).getContent());

        return iNode;
    }

    private void printBlock(int blockNumber) {
        System.out.println(disk.getBlocks().get(blockNumber).getContent()
                .substring(1, getLastCharacterOfBlockOrDash(blockNumber))
        );
    }

    private int getLastCharacterOfBlockOrDash(int blockNumber) {
        return disk.getBlocks().get(blockNumber).getContent().indexOf('-') == -1 ?
                disk.getBlocks().get(blockNumber).getContent().length() :
                disk.getBlocks().get(blockNumber).getContent().indexOf('-');
    }

    private Runnable format() {

        return () -> {
            disk.setBlocks(disk.getBlocks().stream().map((Block block) -> {
                block.setContent("-".repeat(blockSize - 1));
                block.setType(BlockType.FREE);
                return block;
            }).collect(Collectors.toList()));
        };
    }

    private Runnable displayDisk() {
        return () -> {
            for (int i = 0; i < disk.getBlocks().size(); i++) {
                System.out.printf("""
                        block %d
                        [%s]
                        """, i, disk.getBlocks().get(i).getContent());
            }
        };
    }

    private Commands getCommand() {
        Scanner sc = new Scanner(System.in);
        return Commands.valueOf(sc.nextLine().toUpperCase(Locale.ROOT));
    }

}
