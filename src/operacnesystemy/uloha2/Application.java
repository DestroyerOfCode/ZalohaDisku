package operacnesystemy.uloha2;

import operacnesystemy.uloha2.data.Disk;
import operacnesystemy.uloha2.service.DiskService;
import operacnesystemy.uloha2.service.IndexNodeService;

import java.util.ArrayList;

public class Application {

    public static void main(String[] args) {
        DiskService diskService = new DiskService(new Disk(new ArrayList<>()), new IndexNodeService());
        diskService.start();
    }
}
