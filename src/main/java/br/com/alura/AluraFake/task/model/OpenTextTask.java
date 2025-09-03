package br.com.alura.AluraFake.task.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OPEN_TEXT")
public class OpenTextTask extends Task {
}