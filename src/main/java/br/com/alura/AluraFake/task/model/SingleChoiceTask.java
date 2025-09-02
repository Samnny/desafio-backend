package br.com.alura.AluraFake.task.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SINGLE_CHOICE")
public class SingleChoiceTask extends ChoiceTask {
}
