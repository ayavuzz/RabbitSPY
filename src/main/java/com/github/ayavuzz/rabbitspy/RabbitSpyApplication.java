package com.github.ayavuzz.rabbitspy;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.application.Application;

@SpringBootApplication
public class RabbitSpyApplication {

  public static void main(String[] args) {
    Application.launch(SpyFXApplication.class, args);
  }

}
