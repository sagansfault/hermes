package com.projecki.hermes;

import java.util.Arrays;
import java.util.Scanner;

public class HermesEntry {

    public static void main(String[] args) {

        Hermes hermes = new Hermes();

        Scanner scanner = new Scanner(System.in);

        System.out.print(" > ");
        while (scanner.hasNext()) {

            String line = scanner.nextLine();
            String[] partsRaw = line.split(" ");
            String base = partsRaw[0];
            String[] cmdArgs = Arrays.copyOfRange(partsRaw, 1, partsRaw.length);

            hermes.getCommandManager().getCommand(base).ifPresentOrElse(command -> command.run(cmdArgs), () -> {
                System.out.println("No command found");
            });
            System.out.print(" > ");
        }
    }
}
