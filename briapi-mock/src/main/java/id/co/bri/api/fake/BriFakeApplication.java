package id.co.bri.api.fake;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BriFakeApplication {

  public static void main(String[] args) {
    SpringApplication.run(BriFakeApplication.class, args);

    System.out.println("Service is operating normal");

    try {
      Scanner scanner = new Scanner(System.in);

      while (true) {
        System.out.print("Type [S]low / [N]ormal to change service behaviour: ");
        String mode = scanner.next().toUpperCase();
        if ("S".equals(mode)) {
          BriFakeRestController.slow = true;
          System.out.println("Service is now slow");
        }

        if ("N".equals(mode)) {
          BriFakeRestController.slow = false;
          System.out.println("Service is back to normal");
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

}
