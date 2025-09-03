package br.com.alura.AluraFake.task.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public abstract class ChoiceTask extends Task {
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Option> options = new ArrayList<>();

    public void addOption(Option option) {
        this.options.add(option);
        option.setTask(this);
    }
}